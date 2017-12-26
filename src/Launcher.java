import graphics.Renderer;
import graphics.Window;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.*;


public class Launcher {
    private GLFWErrorCallback errorCallback;
    private final Object lock = new Object();
    private Window window;
    private Renderer renderer;

    private void run() {
        try {
            init();
            loop();

            synchronized (lock) {
                window.delete();
                renderer.delete();
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

        window = new Window(640, 480, "WorldEngine", true, 2, false);
    }

    private void rendererThread() {

        renderer = new Renderer();
        window.setContext();
        renderer.init();

        long lastTime = System.nanoTime();

        Quaternionf q = new Quaternionf();

        while (!window.isDestroyed()) {
            long thisTime = System.nanoTime();
            float dt = (thisTime - lastTime) / 1E9f;
            lastTime = thisTime;

            Vector2f cursorPos = window.getCursorPosition();
            Matrix4f model = new Matrix4f();
            model.rotateX(-cursorPos.y * 10);
            model.rotateY(-cursorPos.x * 10);
            renderer.updateModelMatrix(model);

            renderer.clear();
            renderer.begin();

            renderer.drawCube();

            renderer.end();

            synchronized (lock) {
                if (!window.isDestroyed()) {
                    window.update();
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
        while (!window.isClosing()) {
            glfwWaitEvents();
        }
    }

    public static void main(String[] args) {
        new Launcher().run();
    }
}