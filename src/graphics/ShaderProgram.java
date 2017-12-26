package graphics;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private final int id;
    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public ShaderProgram() {
        id = glCreateProgram();
    }

    public void attachShader(Shader shader) {
        glAttachShader(id, shader.getId());
    }

    public void link() {
        glLinkProgram(id);
        checkStatus();
    }

    public int getAttributeLocation(CharSequence name) {
        return glGetAttribLocation(id, name);
    }

    public void enableVertexAttribute(int location) {
        glEnableVertexAttribArray(location);
    }

    public void disableVertexAttribute(int location) {
        glDisableVertexAttribArray(location);
    }

    public void pointVertexAttribute(int location, int size, int stride, int offset) {
        glVertexAttribPointer(location, size, GL_FLOAT, false, stride, offset);
    }

    public int getUniformLocation(CharSequence name) {
        return glGetUniformLocation(id, name);
    }

    public void setUniform(int location, Matrix4f value) {
        glUniformMatrix4fv(location, false, value.get(matrixBuffer));
    }

    public void use() {
        glUseProgram(id);
    }

    public void checkStatus() {
        int status = glGetProgrami(id, GL_LINK_STATUS);
        if (status != GL_TRUE) {
            throw new RuntimeException(glGetProgramInfoLog(id));
        }
    }

    public void delete() {
        glDeleteProgram(id);
        MemoryUtil.memFree(matrixBuffer);
    }

}
