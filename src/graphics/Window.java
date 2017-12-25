package graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

import org.lwjgl.opengl.GL;


public class Window {
    private final long id;
    private boolean fullscreen;
    private boolean vsync;
    private int width, height, msaa;
    private boolean destroyed;

    private float mouseX, mouseY;
    private boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST + 1];

    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWCursorPosCallback cursorPosCallback;

    public Window(int width, int height, CharSequence title, boolean vsync, int msaa, boolean fullscreen) {
        this.width = width;
        this.height = height;
        this.vsync = vsync;
        this.msaa = msaa;
        this.fullscreen = fullscreen;
        this.destroyed = false;

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        if (msaa > 0)
            glfwWindowHint(GLFW_SAMPLES, msaa);

        long monitor = glfwGetPrimaryMonitor();

        GLFWVidMode videoMode = glfwGetVideoMode(monitor);
        if (fullscreen) {
            width = videoMode.width();
            height = videoMode.height();
        }

        id = glfwCreateWindow(width, height, title, fullscreen ? monitor : NULL, NULL);
        if (id == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(id, keyCallback = new GLFWKeyCallback() {
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
        glfwSetCursorPosCallback(id, cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                mouseX = (float) xpos;
                mouseY = (float) ypos;
            }
        });

        if (fullscreen) {
            glfwSetWindowPos(id, (videoMode.width() - width) / 2, (videoMode.height() - height) / 2);
        }

        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(id, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
        this.width = framebufferSize.get(0);
        this.height = framebufferSize.get(1);

        setContext();

        if (vsync)
            glfwSwapInterval(1);

        glfwShowWindow(id);
    }

    public void setContext() {
        glfwMakeContextCurrent(id);
        GL.createCapabilities();

    }

    public boolean isClosing() {
        return glfwWindowShouldClose(id);
    }

    public void update() {
        glfwSwapBuffers(id);
    }

    public void destroy() {
        destroyed = true;
        glfwDestroyWindow(id);
        keyCallback.free();
        cursorPosCallback.free();
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
