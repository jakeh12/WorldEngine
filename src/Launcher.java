import graphics.Shader;

import graphics.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Launcher {
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWFramebufferSizeCallback framebufferSizeCallback;

    private long window;
    private int width = 800;
    private int height = 600;
    private final Object lock = new Object();
    private boolean destroyed;
    public final boolean windowed = true;


    private float mouseX, mouseY;
    private boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST + 1];


    private Matrix4f viewProjMatrix = new Matrix4f();
    private FloatBuffer fb = BufferUtils.createFloatBuffer(16);

    private void run() {
        try {
            init();
            loop();

            synchronized (lock) {
                destroyed = true;
                glfwDestroyWindow(window);
            }
            keyCallback.free();
            cursorPosCallback.free();
            framebufferSizeCallback.free();
        } finally {
            glfwTerminate();
            errorCallback.free();
        }
    }

    private void init() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 1);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        long monitor = glfwGetPrimaryMonitor();

        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
        if (!windowed) {
            width = vidmode.width();
            height = vidmode.height();
        }

        window = glfwCreateWindow(width, height, "WorldEngine", !windowed ? monitor : NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);

                if (action == GLFW_PRESS || action == GLFW_REPEAT)
                    keyDown[key] = true;
                else
                    keyDown[key] = false;
            }
        });
        glfwSetFramebufferSizeCallback(window, framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        });
        glfwSetCursorPosCallback(window, cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                mouseX = (float) xpos / width * 2.0f;
                mouseY = (float) ypos / height * 2.0f;
                System.out.println("X: " + mouseX + " Y: " + mouseY);
            }
        });

        if (windowed) {
            glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
        }

        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
        width = framebufferSize.get(0);
        height = framebufferSize.get(1);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);
    }

    private void buildCube() {
        // create buffer to hold the vertex colors
        FloatBuffer cb = BufferUtils.createFloatBuffer(3 * 4 * 6);
        // create buffer to hold the vertex positions
        FloatBuffer pb = BufferUtils.createFloatBuffer(3 * 4 * 6);
        // define all faces of a cube as quads.
        // this has redundant vertices in it, but it's easy to create
        // an element buffer for it.
        for (int i = 0; i < 4; i++)
            cb.put(0.0f).put(0.0f).put(0.2f);
        pb.put(0.5f).put(-0.5f).put(-0.5f);
        pb.put(-0.5f).put(-0.5f).put(-0.5f);
        pb.put(-0.5f).put(0.5f).put(-0.5f);
        pb.put(0.5f).put(0.5f).put(-0.5f);
        for (int i = 0; i < 4; i++)
            cb.put(0.0f).put(0.0f).put(1.0f);
        pb.put(0.5f).put(-0.5f).put(0.5f);
        pb.put(0.5f).put(0.5f).put(0.5f);
        pb.put(-0.5f).put(0.5f).put(0.5f);
        pb.put(-0.5f).put(-0.5f).put(0.5f);
        for (int i = 0; i < 4; i++)
            cb.put(1.0f).put(0.0f).put(0.0f);
        pb.put(0.5f).put(-0.5f).put(-0.5f);
        pb.put(0.5f).put(0.5f).put(-0.5f);
        pb.put(0.5f).put(0.5f).put(0.5f);
        pb.put(0.5f).put(-0.5f).put(0.5f);
        for (int i = 0; i < 4; i++)
            cb.put(0.2f).put(0.0f).put(0.0f);
        pb.put(-0.5f).put(-0.5f).put(0.5f);
        pb.put(-0.5f).put(0.5f).put(0.5f);
        pb.put(-0.5f).put(0.5f).put(-0.5f);
        pb.put(-0.5f).put(-0.5f).put(-0.5f);
        for (int i = 0; i < 4; i++)
            cb.put(0.0f).put(1.0f).put(0.0f);
        pb.put(0.5f).put(0.5f).put(0.5f);
        pb.put(0.5f).put(0.5f).put(-0.5f);
        pb.put(-0.5f).put(0.5f).put(-0.5f);
        pb.put(-0.5f).put(0.5f).put(0.5f);
        for (int i = 0; i < 4; i++)
            cb.put(0.0f).put(0.2f).put(0.0f);
        pb.put(0.5f).put(-0.5f).put(-0.5f);
        pb.put(0.5f).put(-0.5f).put(0.5f);
        pb.put(-0.5f).put(-0.5f).put(0.5f);
        pb.put(-0.5f).put(-0.5f).put(-0.5f);
        pb.flip();
        cb.flip();
        // build element buffer
        IntBuffer eb = BufferUtils.createIntBuffer(6 * 6);
        for (int i = 0; i < 4 * 6; i += 4)
            eb.put(i).put(i + 1).put(i + 2).put(i + 2).put(i + 3).put(i);
        eb.flip();
        // setup vertex positions buffer
        int cubeVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cubeVbo);
        glBufferData(GL_ARRAY_BUFFER, pb, GL_STATIC_DRAW);
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(3, GL_FLOAT, 0, 0);
        // setup vertex color buffer
        int cubeCb = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cubeCb);
        glBufferData(GL_ARRAY_BUFFER, cb, GL_STATIC_DRAW);
        glEnableClientState(GL_COLOR_ARRAY);
        glColorPointer(3, GL_FLOAT, 0, 0);
        // setup element buffer
        int cubeEbo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, cubeEbo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, eb, GL_STATIC_DRAW);
    }

    private void renderGrid() {
        glBegin(GL_LINES);
        for (int i = -20; i <= 20; i++) {
            glVertex3f(-20.0f, 0.0f, i);
            glVertex3f(20.0f, 0.0f, i);
            glVertex3f(i, 0.0f, -20.0f);
            glVertex3f(i, 0.0f, 20.0f);
        }
        glEnd();
    }

    private void initOpenGLAndRenderInAnotherThread() {
        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        GL.createCapabilities();

        glClearColor(0.6f, 0.7f, 0.8f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        // Create a simple shader program
        Shader vertexShader = Shader.loadShader(GL_VERTEX_SHADER, "shaders/vshader.glsl");
        Shader fragmentShader = Shader.loadShader(GL_FRAGMENT_SHADER, "shaders/fshader.glsl");

        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.attachShader(vertexShader);
        shaderProgram.attachShader(fragmentShader);
        shaderProgram.link();
        shaderProgram.use();

        // Obtain uniform location
        int matLocation = shaderProgram.getUniformLocation("viewProjMatrix");
        int colorLocation = shaderProgram.getUniformLocation("color");
        long lastTime = System.nanoTime();

        /* Quaternion to rotate the cube */
        Quaternionf q = new Quaternionf();

        buildCube();

        while (!destroyed) {
            long thisTime = System.nanoTime();
            float dt = (thisTime - lastTime) / 1E9f;
            lastTime = thisTime;


            glViewport(0, 0, width, height);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Create a view-projection matrix
            viewProjMatrix.setPerspective((float) Math.toRadians(45.0f), (float) width / height, 0.01f, 100.0f)
                    .lookAt(0.0f, 4.0f, 10.0f,
                            0.0f, 0.5f, 0.0f,
                            0.0f, 1.0f, 0.0f);
            // Upload the matrix stored in the FloatBuffer to the
            // shader uniform.
            glUniformMatrix4fv(matLocation, false, viewProjMatrix.get(fb));
            // Render the grid without rotating
            glUniform3f(colorLocation, 0.3f, 0.3f, 0.3f);
            renderGrid();

            // rotate the cube (45 degrees per second)
            // and translate it by 0.5 in y
            viewProjMatrix.translate(0.0f, 0.5f, 0.0f)
                    .rotate(q.rotateY((float) Math.toRadians(45) * dt).normalize());
            // Upload the matrix
            glUniformMatrix4fv(matLocation, false, viewProjMatrix.get(fb));

            // Render solid cube with outlines
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            glUniform3f(colorLocation, 0.6f, 0.7f, 0.8f);


            glDrawElements(GL_TRIANGLES, 6 * 6, GL_UNSIGNED_INT, 0L);

            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glEnable(GL_POLYGON_OFFSET_LINE);
            glPolygonOffset(-1.f, -1.f);
            glUniform3f(colorLocation, 0.0f, 0.0f, 0.0f);
            glDrawElements(GL_TRIANGLES, 6 * 6, GL_UNSIGNED_INT, 0L);
            glDisable(GL_POLYGON_OFFSET_LINE);

            synchronized (lock) {
                if (!destroyed) {
                    glfwSwapBuffers(window);
                }
            }
        }
    }

    private void loop() {
        /*
         * Spawn a new thread which to make the OpenGL context current in and which does the
         * rendering.
         */
        new Thread(this::initOpenGLAndRenderInAnotherThread).start();

        /* Process window messages in the main thread */
        while (!glfwWindowShouldClose(window)) {
            glfwWaitEvents();
        }
    }

    public static void main(String[] args) {
        new Launcher().run();
    }
}