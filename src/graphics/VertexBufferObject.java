package graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;

public class VertexBufferObject {
    private final int id;

    public VertexBufferObject() {
        id = glGenBuffers();
    }

    public int getId() {
        return id;
    }

    public void bind(int target) {
        glBindBuffer(target, id);
    }

    public void uploadData(int target, FloatBuffer data, int usage) {
        glBufferData(target, data, usage);
    }

    public void delete() {
        glDeleteBuffers(id);
    }
}
