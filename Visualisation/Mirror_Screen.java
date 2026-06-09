package Visualisation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.javacpp.BytePointer;
import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import org.bytedeco.ffmpeg.swscale.SwsContext;
import static org.bytedeco.ffmpeg.global.swscale.*;
import static org.bytedeco.ffmpeg.presets.avutil.AVERROR_EAGAIN;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;


public class Mirror_Screen {

    private static final String ADB_PATH = "adb"; // Or full path to adb
    private static final String SCRCPY_SERVER_JAR_PATH = "lib/scrcpy-server.jar";
    private static final String DEVICE_SERVER_PATH = "/data/local/tmp/scrcpy-server.jar";
    private static final String DEVICE_SOCKET_NAME = "scrcpy"; // Default socket name
    private static final int LOCAL_FORWARD_PORT = 27184; // Default Scrcpy port
    private static final String SCRCPY_VERSION = "3.2"; // Match the server version if possible
    private static final int CONNECT_TIMEOUT_MS = 2000; // Connection timeout
    private static final int CONNECT_RETRY_DELAY_MS = 1000;
    private static final int CONNECT_MAX_ATTEMPTS = 10;
    private static final int ADB_WAIT_TIMEOUT_S = 2; // Timeout for ADB commands like forward/remove

    // Scrcpy protocol constants
    private static final long PACKET_FLAG_CONFIG = 1L << 63;
    private static final long PACKET_FLAG_KEY_FRAME = 1L << 62;
    private static final long PACKET_PTS_MASK = PACKET_FLAG_KEY_FRAME - 1;

    // --- FFmpeg Decoder Members ---
    private AVCodec codec = avcodec_find_decoder(AV_CODEC_ID_H264);
    private volatile AVCodecContext codec_context = avcodec_alloc_context3(codec); // The context currently used for decoding
    private AVPacket packet = av_packet_alloc();
    private AVFrame decodedFrame = av_frame_alloc();
    private BytePointer extra_data_Pointer = null;

    // --- SwScale Members ---
    private SwsContext swsContext = null;
    private AVFrame rgbFrame = av_frame_alloc();
    private BytePointer rgbBufferPtr = null;
    private int targetPixFormat = AV_PIX_FMT_BGR24;
    private int sourcePixFormat = -1;
    private int video_width = -1;
    private int video_height = -1;
    private double max_window_height = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;

    // --- JavaCV Frame for Processing (Keep as before) ---
    private Frame processedFrame = new Frame();

    // --- AWT/Swing Display Members (Keep as before) ---
    private JFrame displayFrame = null;
    private ImagePanel imagePanel = null;
    private Java2DFrameConverter java2DConverter = new Java2DFrameConverter(); 
    private final AtomicReference<BufferedImage> currentImage = new AtomicReference<>();
    private volatile Dimension lastPanelPrefSize = new Dimension(0, 0);

    private volatile boolean stopped = false; // volatile as it's checked in loops and set in another thread (shutdown hook)
    private Process serverProcess = null;
    private Process forwardProcess = null;
    private Socket clientSocket = null;
    private ExecutorService streamReaderExecutor = null;

    
    
    public static void main(String[] args) {
        File serverJar = new File(SCRCPY_SERVER_JAR_PATH);
        if (!serverJar.exists() || !serverJar.isFile()) {
            System.err.println("FATAL ERROR: scrcpy-server.jar not found at specified path: " + SCRCPY_SERVER_JAR_PATH);
            System.err.println("Please ensure the path is correct and the file exists.");
            System.exit(1); // Exit if essential file is missing
        }

        Mirror_Screen client = new Mirror_Screen();
        Runtime.getRuntime().addShutdownHook(new Thread(client::stopClient, "ScrcpyShutdownHook"));
        client.start();
    }

    /**
     * Starts the client process: pushes server, starts server, forwards port, connects, processes stream.
     */
    public void start() {
        streamReaderExecutor = Executors.newCachedThreadPool();
        try {
            System.out.println("Starting Scrcpy client...");

            // Execute ADB commands and connect
            if (!pushServerJar()) return;
            serverProcess = startServerProcess();
            if (serverProcess == null) return;

            // Start reading server's output streams immediately
            startStreamReader(serverProcess.getErrorStream(), "Server stderr");
            startStreamReader(serverProcess.getInputStream(), "Server stdout");

            System.out.println("Waiting a few seconds for server to initialize on device...");
            Thread.sleep(3000); // Adjust if server needs more/less time

            if (!setupAdbForward()) return; 
            clientSocket = connectToLocalPort();
            if (clientSocket == null) return; 
            System.out.println("Successfully connected to server via forwarded port " + LOCAL_FORWARD_PORT);

            // Begin processing the video stream from the server
            processStream(clientSocket.getInputStream());

        } catch (IOException e) {
            if (!stopped) { 
                System.err.println("IOException during client setup or stream processing: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Client execution interrupted.");
        } catch (Exception e) {
            if (!stopped) {
                System.err.println("Unexpected error during client execution: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            stopClient();
        }
        System.out.println("Scrcpy client finished execution.");
    }

    /**
     * Pushes the scrcpy-server.jar to the device.
     * @return true if successful, false otherwise.
     */
    private boolean pushServerJar() {
        System.out.println("Pushing server JAR to device: " + SCRCPY_SERVER_JAR_PATH + " -> " + DEVICE_SERVER_PATH);
        Process pushProcess = null;
        try {
            pushProcess = new ProcessBuilder(ADB_PATH, "push", SCRCPY_SERVER_JAR_PATH, DEVICE_SERVER_PATH).start();
            // Read streams concurrently to prevent process blocking
            startStreamReader(pushProcess.getErrorStream(), "Push stderr");
            startStreamReader(pushProcess.getInputStream(), "Push stdout");

            boolean exited = pushProcess.waitFor(15, TimeUnit.SECONDS); // Generous timeout for push
            if (!exited) {
                System.err.println("ADB push command timed out.");
                pushProcess.destroyForcibly();
                return false;
            }

            int exitCode = pushProcess.exitValue();
            if (exitCode != 0) {
                System.err.println("Failed to push server JAR (Exit code: " + exitCode + ")");
                System.err.println("Please check ADB connection, device storage, and JAR path: " + SCRCPY_SERVER_JAR_PATH);
                return false;
            }
            System.out.println("Server JAR pushed successfully.");
            return true;
        } catch (IOException e) {
            System.err.println("IOException during ADB push: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("ADB push interrupted.");
            if (pushProcess != null) pushProcess.destroyForcibly();
            return false;
        }
    }

    /**
     * Starts the scrcpy server process on the device via adb shell.
     * @return The Process object if successful, null otherwise.
     */
    private Process startServerProcess() {
        // Construct server command arguments
        List<String> serverCommand = new ArrayList<>();
        serverCommand.add(ADB_PATH);
        serverCommand.add("shell");
        serverCommand.add("CLASSPATH=" + DEVICE_SERVER_PATH);
        serverCommand.add("app_process");
        serverCommand.add("/"); // Base directory argument for app_process
        serverCommand.add("com.genymobile.scrcpy.Server");
        serverCommand.add(SCRCPY_VERSION);
        serverCommand.add("log_level=debug"); // Default log level
        serverCommand.add("video_codec=h264");
        serverCommand.add("audio_codec=opus"); // Effectively ignored due to audio=false
        serverCommand.add("video_bit_rate=10000000"); // Example: 8 Mbps
        serverCommand.add("max_size=0"); // 0 means device native resolution
        serverCommand.add("max_fps=0");  // 0 means default/unlimited FPS
        serverCommand.add("tunnel_forward=true"); // Server listens for incoming connection
        serverCommand.add("control=false"); // Disable control channel
        serverCommand.add("audio=false"); // Disable audio stream
        serverCommand.add("video=true"); // Enable video stream
        serverCommand.add("send_device_meta=true"); // Send device name first
        serverCommand.add("send_frame_meta=true"); // Send 12-byte header before each packet
        serverCommand.add("send_dummy_byte=true"); // Send byte on connect confirmation

        System.out.println("Starting server on device with command: " + String.join(" ", serverCommand));
        try {
            Process process = new ProcessBuilder(serverCommand).start();
            // Check immediately if process started, though it might crash later
            if (!process.isAlive() && process.exitValue() != 0) {
                System.err.println("Server process failed to start immediately. Check ADB connection and server arguments.");
                printStream(process.getErrorStream(), "Server stderr (immediate fail)");
                printStream(process.getInputStream(), "Server stdout (immediate fail)");
                return null;
            }
            System.out.println("Server process launched (PID might be available via logs).");
            return process;
        } catch (IOException e) {
            System.err.println("IOException trying to start server process: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sets up ADB port forwarding from a local TCP port to the device's abstract socket.
     * @return true if successful, false otherwise.
     */
    private boolean setupAdbForward() {
        // Attempt to remove any existing forward rule first
        try {
            Process removeFwd = new ProcessBuilder(ADB_PATH, "forward", "--remove", "tcp:" + LOCAL_FORWARD_PORT).start();
            // Don't wait too long, error if it hangs
            if (!removeFwd.waitFor(ADB_WAIT_TIMEOUT_S, TimeUnit.SECONDS)) {
                removeFwd.destroyForcibly();
                System.err.println("Warning: Timeout removing existing adb forward rule (continuing anyway).");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Warning: Error removing existing adb forward rule: " + e.getMessage() + " (continuing anyway).");
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        }

        System.out.println("Setting up ADB forward: tcp:" + LOCAL_FORWARD_PORT + " -> localabstract:" + DEVICE_SOCKET_NAME);
        try {
            forwardProcess = new ProcessBuilder(ADB_PATH, "forward", "tcp:" + LOCAL_FORWARD_PORT, "localabstract:" + DEVICE_SOCKET_NAME).start();

            startStreamReader(forwardProcess.getErrorStream(), "Forward stderr");
            startStreamReader(forwardProcess.getInputStream(), "Forward stdout");

            // ADB forward usually exits quickly (0 for success, non-zero for error)
            boolean exited = forwardProcess.waitFor(ADB_WAIT_TIMEOUT_S, TimeUnit.SECONDS);
            if (!exited) {
                // If it doesn't exit, ADB might keep it running in the background (less common now)
                System.out.println("ADB forward command still running (assuming success).");
                // don't own this process fully if it backgrounds, cleanup might be harder.
                return true;
            }

            int exitCode = forwardProcess.exitValue();
            if (exitCode != 0) {
                System.err.println("Failed to set up ADB forward (Exit code: " + exitCode + ")");
                System.err.println("Check if another process is using local port " + LOCAL_FORWARD_PORT + " or if adb server is running correctly.");
                return false;
            }
            System.out.println("ADB forward setup successful.");
            return true;

        } catch (IOException e) {
            System.err.println("IOException during ADB forward setup: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("ADB forward setup interrupted.");
            if(forwardProcess != null) forwardProcess.destroyForcibly();
            return false;
        }
    }

    /**
     * Connects to the locally forwarded port with retries.
     * @return The connected Socket object, or null if connection fails.
     */
    private Socket connectToLocalPort() {
        System.out.println("Attempting to connect to localhost:" + LOCAL_FORWARD_PORT + "...");
        Socket socket = null;
        for (int attempt = 1; attempt <= CONNECT_MAX_ATTEMPTS && !stopped; attempt++) {
            try {
                socket = new Socket();
                // Set options before connecting
                socket.setSoTimeout(5000); 
                socket.connect(new InetSocketAddress("localhost", LOCAL_FORWARD_PORT), CONNECT_TIMEOUT_MS);
                socket.setTcpNoDelay(true); // Improve latency for streaming
                System.out.println("Connection attempt " + attempt + " successful.");
                return socket;
            } catch (SocketTimeoutException e) {
                System.err.println("Connection attempt " + attempt + " timed out after " + CONNECT_TIMEOUT_MS + "ms.");
            } catch (IOException e) {
                System.err.println("Connection attempt " + attempt + " failed: " + e.getMessage());
            }

            // Wait before retrying if not the last attempt and not stopped
            if (attempt < CONNECT_MAX_ATTEMPTS && !stopped) {
                System.out.println("Retrying in " + CONNECT_RETRY_DELAY_MS + "ms...");
                try {
                    Thread.sleep(CONNECT_RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.err.println("Connection retry interrupted.");
                    try {
                        socket.close();
                        return null;
                    } catch (IOException e) {
                        System.err.println("Error closing socket: " + e.getMessage());
                    }
                }
            }
        }

        System.err.println("Failed to connect to localhost:" + LOCAL_FORWARD_PORT + " after " + CONNECT_MAX_ATTEMPTS + " attempts.");
        return null;
    }

    /**
     * Reads the stream from the server, parses headers and packets, decodes, and processes frames.
     * @param inputStream The InputStream from the connected socket.
     */
    private void processStream(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        try {
            // Read Dummy Byte
            try { 
                dataInputStream.readByte(); 
                System.out.println("Read dummy byte confirmation."); 
            } catch (IOException e) {
                /* ... error handling ... */ 
                throw e; 
            }

            // Read Device Metadata
            byte[] deviceMeta = new byte[64]; 
            dataInputStream.readFully(deviceMeta);
            String deviceName = new String(deviceMeta, StandardCharsets.UTF_8).trim();
            System.out.println("Connected to device: " + deviceName);

            // Read Video Header
            int codecId = dataInputStream.readInt(); 
            System.out.println("Received video codec ID: " + codecId);
            video_width = dataInputStream.readInt(); 
            video_height = dataInputStream.readInt();
            System.out.printf("Received video stream header: Codec=0x%x, Size=%dx%d\n", codecId, video_width, video_height);

            double scale = Math.min(max_window_height/video_width, max_window_height/video_height);
            int scaledWidth = (int)(video_width*scale);
            int scaledHeight = (int)(video_height*scale);

            final Dimension targetPanelSize = new Dimension(scaledWidth,scaledHeight);

            try {
                SwingUtilities.invokeAndWait(() -> {
                            
                    displayFrame = new JFrame("Screen Stream");
                    displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    
                    imagePanel = new ImagePanel();
                    imagePanel.setPreferredSize(targetPanelSize);

                    displayFrame.add(imagePanel);
                    displayFrame.pack();
                    displayFrame.setLocationRelativeTo(null);
                    displayFrame.setVisible(true);

                    lastPanelPrefSize = targetPanelSize;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Read Packet Stream Loop
            ByteBuffer headerBuffer = ByteBuffer.allocate(12); 
            headerBuffer.order(ByteOrder.BIG_ENDIAN);

            System.out.println("Starting packet reading loop...");
            while (!stopped) {
                try {
                    dataInputStream.readFully(headerBuffer.array()); 
                    headerBuffer.rewind();
                    long ptsAndFlags = headerBuffer.getLong(); 
                    int packetSize = headerBuffer.getInt();
                    boolean is_config = (ptsAndFlags & PACKET_FLAG_CONFIG) != 0;
                    boolean is_key_frame = (ptsAndFlags & PACKET_FLAG_KEY_FRAME) != 0;

                    long pts = is_config ? -1 : (ptsAndFlags & PACKET_PTS_MASK);
                    
                    byte[] packet_data = new byte[packetSize]; 
                    dataInputStream.readFully(packet_data);

                    // --- Feed the decoder ---
                    handlePacketData(packet_data, pts, is_config, is_key_frame);

                } catch (Exception e) { 
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    break; 
                }
            }
            System.out.println("Exited packet reading loop.");

        } finally {
            stopClient();
        }
    }


    // --- Actual Decoding and Conversion Logic ---
    /**
     * Handles incoming packet data: decodes, converts format, and processes the frame.
     * @param data The raw encoded packet data.
     * @param pts Presentation timestamp.
     * @param isConfig True if this is a codec config packet (SPS/PPS).
     * @param isKeyFrame True if this packet represents a keyframe.
     * @param streamWidth Width of the video stream from header.
     * @param streamHeight Height of the video stream from header.
     */
    private void handlePacketData(byte[] data, long pts, boolean isConfig, boolean isKeyFrame) {
        if (stopped) return;
        int ret;

        try {
            if (isConfig) {
                // --- Handle Config Packet (SPS/PPS) ---
                System.out.println(" -> Received codec config packet (SPS/PPS), size: " + data.length);
                sws_freeContext(swsContext);
                av_frame_unref(decodedFrame); 
                av_free(rgbBufferPtr);

                avcodec_free_context(codec_context);
                codec_context = avcodec_alloc_context3(codec); // Allocate new context
                
                codec_context.flags(codec_context.flags() | AV_CODEC_FLAG_LOW_DELAY);
                codec_context.thread_count(0);

                extra_data_Pointer = new BytePointer(av_malloc(data.length + AV_INPUT_BUFFER_PADDING_SIZE));

                extra_data_Pointer.put(data, 0, data.length); 
                extra_data_Pointer.position(data.length);
                for (int i = 0; i < AV_INPUT_BUFFER_PADDING_SIZE; ++i) extra_data_Pointer.put((byte) 0);
                extra_data_Pointer.position(0);

                codec_context.extradata(extra_data_Pointer); // Assign pointer - new context now owns it
                codec_context.extradata_size(data.length);

                // Open the NEW context
                ret = avcodec_open2(codec_context, codec, (AVDictionary) null);
                if (ret < 0) {
                    System.err.println("FATAL: Could not open NEW codec context: " + avErrorToString(ret));
                    avcodec_free_context(codec_context); // Free the context we just allocated
                    stopClient(); 
                    return;
                }
                System.out.println("New codec context opened successfully for new config.");


                // Invalidate SwsContext and dimensions forcing re-init on next frame
                // if (swsContext != null && !swsContext.isNull()) { 
                //     sws_freeContext(swsContext); 
                //     swsContext = null; 
                // }

            } else { // --- Process Frame Data Packet ---
                // System.out.println(" -> Received frame data packet, size: " + data.length + ", PTS: " + pts + ", KeyFrame: " + isKeyFrame);

                av_packet_unref(packet); 
                ret = av_new_packet(packet, data.length); 
                if (ret < 0) return;
                packet.data().put(data, 0, data.length); 
                packet.pts(pts); 
                packet.dts(pts); 
                if (isKeyFrame) packet.flags(packet.flags() | AV_PKT_FLAG_KEY);

                // Send Packet to CURRENT Decoder
                ret = avcodec_send_packet(codec_context, packet);
                if (ret < 0 && ret != AVERROR_EAGAIN() && ret != AVERROR_EOF()) { 
                    System.err.println("Error sending packet: "+avErrorToString(ret)); 
                    return; 
                }

                // Receive Decoded Frames from CURRENT Decoder
                while (!stopped) {
                    ret = avcodec_receive_frame(codec_context, decodedFrame);
                    if (ret == AVERROR_EAGAIN()) 
                        break; // Need more input for CURRENT context
                    else if (ret == AVERROR_EOF()) { 
                        stopClient(); 
                        break; 
                    }
                    else if (ret < 0) { 
                        System.err.println("\nError receiving frame: " + avErrorToString(ret)); 
                        break; 
                    }

                    video_width = decodedFrame.width(); 
                    video_height = decodedFrame.height(); 
                    sourcePixFormat = decodedFrame.format();

                    double scale = 0.9 * Math.min(max_window_height/(video_width), max_window_height/(video_height));
                    int scaledWidth = (int)(video_width*scale);
                    int scaledHeight = (int)(video_height*scale);

                    final Dimension targetPanelSize = new Dimension(scaledWidth,scaledHeight);

                    if (!lastPanelPrefSize.equals(targetPanelSize)) {

                        try {
                            SwingUtilities.invokeLater(() -> { 
                                if (imagePanel != null && displayFrame != null) {
                                    imagePanel.setPreferredSize(targetPanelSize);
                                    displayFrame.pack();
                                }
                            });
                            lastPanelPrefSize = targetPanelSize;

                        } catch (Exception e) { 
                            System.err.println("Error scheduling display window resize: " + e.getMessage()); 
                        }

                        // sws_freeContext(swsContext);
                        // av_frame_unref(decodedFrame); 
                        // av_free(rgbBufferPtr); 
                        swsContext = sws_getContext(video_width, video_height, sourcePixFormat, video_width, video_height, targetPixFormat, SWS_BILINEAR, null, null, (double[]) null);

                        int numBytes = av_image_get_buffer_size(targetPixFormat, video_width, video_height, 1); 
                        rgbBufferPtr = new BytePointer(av_malloc(numBytes)); 

                        rgbFrame.width(video_width); 
                        rgbFrame.height(video_height); 
                        rgbFrame.format(targetPixFormat);

                        ret = av_image_fill_arrays(rgbFrame.data(), rgbFrame.linesize(), rgbBufferPtr, targetPixFormat, video_width, video_height, 1); 

                    }
                    
                    ret = sws_scale(swsContext, decodedFrame.data(), decodedFrame.linesize(), 0, video_height, rgbFrame.data(), rgbFrame.linesize());
                    if (ret < 0) { 
                        System.err.println("Error: sws_scale failed: " + avErrorToString(ret)); 
                        av_frame_unref(decodedFrame); 
                        continue; 
                    }

                    try {
                        if (processedFrame.image == null || processedFrame.image.length == 0) 
                            processedFrame.image = new java.nio.Buffer[1];

                        int bufferLimit = rgbFrame.linesize(0) * video_height; 

                        ByteBuffer rgbByteBuffer = rgbFrame.data(0).position(0).limit(bufferLimit).asByteBuffer();
                        processedFrame.imageWidth = video_width; 
                        processedFrame.imageHeight = video_height; 
                        processedFrame.imageDepth = Frame.DEPTH_UBYTE; 
                        processedFrame.imageChannels = 3; 
                        processedFrame.imageStride = rgbFrame.linesize(0); 
                        processedFrame.image[0] = rgbByteBuffer;

                        processDecodedFrame(processedFrame);
                    } catch (Exception e) { 
                        throw e;
                    } finally { 
                        av_frame_unref(decodedFrame); 
                    }
                }
            } 
        } catch (Exception e) { 
            System.out.println(e.getMessage());
            e.printStackTrace(); 
        } finally { 
            av_packet_unref(packet);
        }
    }

    /**
     * Processes the decoded and converted (BGR) frame.
     * Updates the display panel, resizing if necessary, and provides access to pixel data.
    */
    private void processDecodedFrame(Frame frame) {
        try {
            BufferedImage bImage = null;
            bImage = java2DConverter.convert(frame); 
            if (bImage == null) {
                throw new Exception("java2DConverter.convert(frame) returned null");
            }
            currentImage.set(bImage);

            SwingUtilities.invokeLater(() -> { 
                imagePanel.repaint(); 
            });

            // --- Accessing Pixel Data (from BufferedImage or original Frame buffer) ---
            // int cx = bImage.getWidth() / 2;
            // int cy = bImage.getHeight() / 2;
            // // Ensure coordinates are valid before accessing pixels
            // if (cx >= 0 && cx < bImage.getWidth() && cy >= 0 && cy < bImage.getHeight()) {
            //     int rgb = bImage.getRGB(cx, cy);
            //     int r = (rgb >> 16) & 0xFF;
            //     int g = (rgb >> 8) & 0xFF;
            //     int b = rgb & 0xFF;
            //     if (frameCounter % 100 == 0) {
            //           System.out.printf("--> Decoded frame %d (%dx%d), Center Pixel (Img): [%d,%d,%d], PTS: %d\n",
            //                   frameCounter, bImage.getWidth(), bImage.getHeight(), r, g, b, pts);
            //     }
            // }
           // --- PLACEHOLDER: Feed data to Neural Network ---
           // Use either the BufferedImage 'bImage' or the 'pixelBuffer' from the 'frame'
           // depending on what your network prefers.
           // yourNeuralNetwork.process(bImage);

        } catch (Exception e) {
            if (!stopped) {
                System.err.println("Unexpected exception in processDecodedFrame: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    // --- Custom JPanel for Aspect-Ratio Preserving Display ---
    class ImagePanel extends JPanel {
        private static final long serialVersionUID = 1L;
        
        public ImagePanel() {
            setBackground(java.awt.Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage img = currentImage.get(); // Get the latest image
            if (img == null) return;
            try {
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                int imgWidth = img.getWidth();
                int imgHeight = img.getHeight();
                
                double scale = Math.min((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);
                int scaledWidth = (int) (imgWidth * scale);
                int scaledHeight = (int) (imgHeight * scale);
                int x = (panelWidth - scaledWidth) / 2;
                int y = (panelHeight - scaledHeight) / 2;

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(img, x, y, scaledWidth, scaledHeight, null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void cleanupDecoder() {
        System.out.println("Cleaning up decoder and converter resources...");

        // Cleanup Java2DConverter
        if (java2DConverter != null) { 
            try { 
                java2DConverter.close(); 
            } catch(Exception e){

            } 
            java2DConverter = null; 
            System.out.println("Java2DConverter closed/nulled."); 
        }
        // Cleanup SwScale resources
        if (swsContext != null && !swsContext.isNull()) { 
            try { 
                sws_freeContext(swsContext); 
            } catch (Exception e) {

            } 
            swsContext = null; 
            System.out.println("SwsContext freed."); 
        }
        if (rgbBufferPtr != null && !rgbBufferPtr.isNull()) { 
            try { 
                av_free(rgbBufferPtr); 
            } catch (Exception e) {

            } 
            rgbBufferPtr = null; 
            System.out.println("RGB buffer freed."); 
        }
        if (rgbFrame != null && !rgbFrame.isNull()) { 
            try { 
                av_frame_free(rgbFrame); 
            } catch (Exception e) {

            } 
            rgbFrame = null; 
            System.out.println("RGB frame freed."); 
        }
        // Cleanup FFmpeg decoder resources
        if (decodedFrame != null && !decodedFrame.isNull()) { 
            try { 
                av_frame_free(decodedFrame); 
            } catch (Exception e) {

            } 
            decodedFrame = null; 
            System.out.println("Decoder frame freed."); 
        }
        if (packet != null && !packet.isNull()) { 
            try { 
                av_packet_free(packet); 
            } catch (Exception e) {

            } 
            packet = null; 
            System.out.println("Decoder packet freed."); 
        }

        // Free BOTH current and potentially old codec contexts
        if (codec_context != null && !codec_context.isNull()) {
            System.out.println("Freeing current codec context.");
            try { 
                avcodec_free_context(codec_context); 
            } catch (Exception e) {

            } // Frees extradata too
            codec_context = null;
        }

        codec = null; // Clear codec reference
        processedFrame = null; // Clear JavaCV frame reference
        System.out.println("Decoder/Converter cleanup complete.");
    }

    // Stops the client, cleans up resources (ADB processes, sockets, threads, decoder).
    public synchronized void stopClient() {
        if (stopped) { 
            System.out.println("\nStopClient already called or in progress."); 
            return; 
        }
        System.out.println("\nInitiating Scrcpy client shutdown sequence...");
        stopped = true; // Signal loops and other operations to stop
        if (displayFrame != null) { 
            try { 
                SwingUtilities.invokeLater(() -> { 
                    if (displayFrame != null) { 
                        displayFrame.setVisible(false); 
                        displayFrame.dispose(); 
                        System.out.println("Display window disposed."); 
                    } 
                }); 
            } catch (Exception e) { 
                System.err.println("Exception requesting JFrame disposal: " + e.getMessage()); 
            } 
            displayFrame = null; 
            imagePanel = null; 
        }
        cleanupDecoder(); // Calls the modified cleanup
        if (clientSocket != null && !clientSocket.isClosed()) { 
            try { 
                if (!clientSocket.isInputShutdown()) {
                    try { 
                        clientSocket.shutdownInput(); 
                    } catch (IOException e) {} 
                }
                
                if (!clientSocket.isOutputShutdown()) {
                    try { 
                        clientSocket.shutdownOutput(); 
                    } catch (IOException e) {} 
                }
                clientSocket.close(); 
                System.out.println("Client socket closed."); 
                
            } catch (IOException e) { 
                System.err.println("Warning: Error closing client socket: " + e.getMessage()); 
            } 
        } 
        clientSocket = null;
        if (forwardProcess != null && forwardProcess.isAlive()) { 
            System.out.println("Stopping ADB forward process..."); 
            forwardProcess.destroyForcibly(); 
        } 
        try { 
            System.out.println("Removing ADB forward rule..."); 
            Process removeForward = new ProcessBuilder(ADB_PATH, "forward", "--remove", "tcp:" + LOCAL_FORWARD_PORT).start(); 
            if (!removeForward.waitFor(ADB_WAIT_TIMEOUT_S, TimeUnit.SECONDS)) { 
                removeForward.destroyForcibly(); 
                System.err.println("Warning: Timeout removing ADB forward rule."); 
            } 
        } catch(Exception e) { 
            System.err.println("Warning: Could not remove ADB forward rule cleanly: " + e.getMessage()); 
            if(e instanceof InterruptedException) Thread.currentThread().interrupt(); 
        } 
        forwardProcess = null;
        if (serverProcess != null && serverProcess.isAlive()) { 
            System.out.println("Stopping server process on device (destroy)..."); 
            serverProcess.destroy(); 
            try { 
                if (!serverProcess.waitFor(3, TimeUnit.SECONDS)) { 
                    System.out.println("Server process did not exit gracefully, forcing destroyForcibly..."); 
                    serverProcess.destroyForcibly(); 
                    serverProcess.waitFor(1, TimeUnit.SECONDS); 
                } 
                if(serverProcess.isAlive()) 
                    System.err.println("Warning: Server process may still be running after forced destroy."); 
                else 
                    System.out.println("Server process stopped with exit code: " + serverProcess.exitValue()); 
            } catch (InterruptedException e) { 
                Thread.currentThread().interrupt(); 
                serverProcess.destroyForcibly(); 
                System.err.println("Interrupted while waiting for server process."); 
            } catch (IllegalThreadStateException e) { 
                System.out.println("Server process already exited."); 
            } catch (Exception e) { 
                System.err.println("Unexpected error stopping server process: " + e.getMessage()); 
                try { 
                    serverProcess.destroyForcibly(); 
                } catch (Exception ignored) {} 
            } 
        } else if (serverProcess != null) { 
            System.out.println("Server process was already stopped."); 
        } 
        serverProcess = null;
        if (streamReaderExecutor != null && !streamReaderExecutor.isShutdown()) { 
            System.out.println("Shutting down stream reader executor..."); 
            streamReaderExecutor.shutdown(); 
            try { 
                if (!streamReaderExecutor.awaitTermination(3, TimeUnit.SECONDS)) { 
                    System.err.println("Stream reader tasks did not finish gracefully, forcing shutdownNow..."); 
                    streamReaderExecutor.shutdownNow(); 
                    if (!streamReaderExecutor.awaitTermination(2, TimeUnit.SECONDS)) 
                        System.err.println("Stream reader executor did not terminate after shutdownNow."); 
                    } else 
                        System.out.println("Stream readers shut down gracefully."); 
                    } catch (InterruptedException e) { 
                        streamReaderExecutor.shutdownNow(); 
                        Thread.currentThread().interrupt(); 
                        System.err.println("Interrupted during stream reader shutdown."); 
                    } 
                } 
                streamReaderExecutor = null;
        System.out.println("Client stop sequence finished.");
    }

    private void startStreamReader(InputStream stream, String prefix) {
        if (stream == null) {
            System.err.println("Warning: Cannot read null stream for prefix [" + prefix + "]"); 
            return; 
        } 
        if (streamReaderExecutor == null || streamReaderExecutor.isShutdown()) { 
            if (stopped) return; 
            System.err.println("Warning: Stream reader executor not available for prefix [" + prefix + "]"); 
            return; 
        } 
        streamReaderExecutor.submit(() -> printStream(stream, prefix)); 
    }
    private void printStream(InputStream stream, String prefix) { 
        java.io.PrintStream out = prefix.toLowerCase().contains("stderr") ? System.err : System.out; 

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) { 
            String line; 
            while ((line = reader.readLine()) != null && !stopped) out.println("[" + prefix + "] " + line); 
            if(stopped) {
                System.out.println("[" + prefix + "] Stream reading stopped by client shutdown."); 
            } else {
                System.out.println("[" + prefix + "] Stream ended (EOF)."); 
            }
        } catch (IOException e) { 
            if (!stopped && !e.getMessage().toLowerCase().contains("stream closed") && !e.getMessage().toLowerCase().contains("pipe closed")) {
                System.err.println("Error reading stream [" + prefix + "]: " + e.getMessage()); 
            } else {
                System.out.println("[" + prefix + "] Stream closed or pipe broken (expected during shutdown or process exit)."); 
            }

        } catch (Exception e) { 
            if (!stopped) { 
                System.err.println("Unexpected error reading stream [" + prefix + "]: " + e.getMessage()); 
                e.printStackTrace(); 
            } 
        } 
    }
    private static String avErrorToString(int errorCode) { 
        byte[] errorBytes = new byte[AV_ERROR_MAX_STRING_SIZE]; 
        int ret = av_strerror(errorCode, errorBytes, errorBytes.length); 
        if (ret < 0) return "Unknown error code " + errorCode + " (av_strerror failed with " + ret + ")"; 
        int len = 0; 
        while (len < errorBytes.length && errorBytes[len] != 0) len++; 
        return new String(errorBytes, 0, len, StandardCharsets.UTF_8) + " (" + errorCode + ")"; 
    }
}

