package graphics;

import java.nio.FloatBuffer;

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

    public void uploadData(int target, long size, int usage) {
        glBufferData(target, size, usage);
    }

    public void uploadSubData(int target, long offset, FloatBuffer data) {
        glBufferSubData(target, offset, data);
    }

    public void delete() {
        glDeleteBuffers(id);
    }
}
