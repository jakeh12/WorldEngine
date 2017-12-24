import graphics.Shader;

import graphics.ShaderProgram;
import graphics.VertexArrayObject;
import graphics.VertexBufferObject;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GLCapabilities;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Launcher {
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWFramebufferSizeCallback framebufferSizeCallback;
    private ShaderProgram shaderProgram;

    private long window;
    private int width = 800;
    private int height = 600;
    private final Object lock = new Object();
    private boolean destroyed;
    public final boolean windowed = true;


    private float mouseX, mouseY;
    private boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST + 1];

    private Matrix4f mvpMatrix = new Matrix4f();
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
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        glfwWindowHint(GLFW_SAMPLES, 4);

        long monitor = glfwGetPrimaryMonitor();

        GLFWVidMode videoMode = glfwGetVideoMode(monitor);
        if (!windowed) {
            width = videoMode.width();
            height = videoMode.height();
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
                mouseX = (float) xpos / width;
                mouseY = (float) ypos / height;
                //System.out.println("X: " + mouseX + " Y: " + mouseY);
            }
        });

        if (windowed) {
            glfwSetWindowPos(window, (videoMode.width() - width) / 2, (videoMode.height() - height) / 2);
        }

        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
        width = framebufferSize.get(0);
        height = framebufferSize.get(1);

        glfwShowWindow(window);
    }

    private void initOpenGLAndRenderInAnotherThread() {
        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        // Create a simple shader program
        Shader vertexShader = Shader.loadShader(GL_VERTEX_SHADER, "shaders/vshader.glsl");
        Shader fragmentShader = Shader.loadShader(GL_FRAGMENT_SHADER, "shaders/fshader.glsl");

        shaderProgram = new ShaderProgram();
        shaderProgram.attachShader(vertexShader);
        shaderProgram.attachShader(fragmentShader);
        shaderProgram.link();
        shaderProgram.use();

        vertexShader.delete();
        fragmentShader.delete();

        // Obtain uniform location
        int mvpLocation = shaderProgram.getUniformLocation("mvpMatrix");
        long lastTime = System.nanoTime();

        /* Quaternion to rotate the cube */
        Quaternionf q = new Quaternionf();

        while (!destroyed) {
            long thisTime = System.nanoTime();
            float dt = (thisTime - lastTime) / 1E9f;
            lastTime = thisTime;


            glViewport(0, 0, width, height);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            mvpMatrix.setPerspective((float) Math.toRadians(45.0f), (float) width / (float) height, 0.01f, 100.0f)
                    .lookAt(0.0f, 2.0f, -3.0f,
                            0.0f, 0.0f, 0.0f,
                            0.0f, 1.0f, 0.0f)
                    .translate(0.0f, 0.0f, 0.0f).rotate(q.rotateY((float) Math.toRadians(45) * dt).normalize()).scale(1.0f, 1.0f, 1.0f);

            glUniformMatrix4fv(mvpLocation, false, mvpMatrix.get(fb));


            FloatBuffer pb = BufferUtils.createFloatBuffer(3 * 6 * 6);
            pb.put(-0.5f).put(-0.5f).put(-0.5f);
            pb.put(-0.5f).put(-0.5f).put(0.5f);
            pb.put(-0.5f).put(0.5f).put(0.5f);
            pb.put(0.5f).put(0.5f).put(-0.5f);
            pb.put(-0.5f).put(-0.5f).put(-0.5f);
            pb.put(-0.5f).put(0.5f).put(-0.5f);

            pb.put(0.5f).put(-0.5f).put(0.5f);
            pb.put(-0.5f).put(-0.5f).put(-0.5f);
            pb.put(0.5f).put(-0.5f).put(-0.5f);
            pb.put(0.5f).put(0.5f).put(-0.5f);
            pb.put(0.5f).put(-0.5f).put(-0.5f);
            pb.put(-0.5f).put(-0.5f).put(-0.5f);

            pb.put(-0.5f).put(-0.5f).put(-0.5f);
            pb.put(-0.5f).put(0.5f).put(0.5f);
            pb.put(-0.5f).put(0.5f).put(-0.5f);
            pb.put(0.5f).put(-0.5f).put(0.5f);
            pb.put(-0.5f).put(-0.5f).put(0.5f);
            pb.put(-0.5f).put(-0.5f).put(-0.5f);

            pb.put(-0.5f).put(0.5f).put(0.5f);
            pb.put(-0.5f).put(-0.5f).put(0.5f);
            pb.put(0.5f).put(-0.5f).put(0.5f);
            pb.put(0.5f).put(0.5f).put(0.5f);
            pb.put(0.5f).put(-0.5f).put(-0.5f);
            pb.put(0.5f).put(0.5f).put(-0.5f);

            pb.put(0.5f).put(-0.5f).put(-0.5f);
            pb.put(0.5f).put(0.5f).put(0.5f);
            pb.put(0.5f).put(-0.5f).put(0.5f);
            pb.put(0.5f).put(0.5f).put(0.5f);
            pb.put(0.5f).put(0.5f).put(-0.5f);
            pb.put(-0.5f).put(0.5f).put(-0.5f);

            pb.put(0.5f).put(0.5f).put(0.5f);
            pb.put(-0.5f).put(0.5f).put(-0.5f);
            pb.put(-0.5f).put(0.5f).put(0.5f);
            pb.put(0.5f).put(0.5f).put(0.5f);
            pb.put(-0.5f).put(0.5f).put(0.5f);
            pb.put(0.5f).put(-0.5f).put(0.5f);
            pb.flip();

            VertexArrayObject vao = new VertexArrayObject();
            vao.bind();
            VertexBufferObject vbo = new VertexBufferObject();
            vbo.bind(GL_ARRAY_BUFFER);
            vbo.uploadData(GL_ARRAY_BUFFER, pb, GL_STATIC_DRAW);
            int posAttribute = shaderProgram.getAttributeLocation("position");
            shaderProgram.enableVertexAttribute(posAttribute);
            shaderProgram.pointVertexAttribute(posAttribute, 3, 0, 0);
            glDrawArrays(GL_TRIANGLES, 0, 36);


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
        new Thread(new Runnable() {
            public void run() {
                initOpenGLAndRenderInAnotherThread();
            }
        }).start();

        /* Process window messages in the main thread */
        while (!glfwWindowShouldClose(window)) {
            glfwWaitEvents();
        }
    }

    public static void main(String[] args) {
        new Launcher().run();
    }
}