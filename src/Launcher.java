import graphics.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;


public class Launcher {
    private GLFWErrorCallback errorCallback;
    private final Object lock = new Object();
    Window mainWindow;

    private void run() {
        try {
            init();
            loop();

            synchronized (lock) {
                mainWindow.destroy();
            }
        } finally {
            glfwTerminate();
            errorCallback.free();
        }
    }

    private void init() {

        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        mainWindow = new Window(640, 480, "WorldEngine", false, 0, false);
    }

    private void rendererThread() {

        Renderer renderer = new Renderer();
        mainWindow.setContext();
        renderer.init();

        long lastTime = System.nanoTime();

        while (!mainWindow.isDestroyed()) {
            long thisTime = System.nanoTime();
            float dt = (thisTime - lastTime) / 1E9f;
            lastTime = thisTime;

            renderer.clear();
            renderer.begin();
            renderer.drawCube();
            renderer.end();

            synchronized (lock) {
                if (!mainWindow.isDestroyed()) {
                    mainWindow.update();
                }
            }
        }
    }

    private void loop() {
        new Thread(new Runnable() {
            public void run() {
                rendererThread();
            }
        }).start();

        /* Process window messages in the main thread */
        while (!mainWindow.isClosing()) {
            glfwWaitEvents();
        }
    }

    public static void main(String[] args) {
        new Launcher().run();
    }
}