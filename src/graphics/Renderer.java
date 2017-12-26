package graphics;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

public class Renderer {
    private VertexArrayObject vao;
    private VertexBufferObject vbo;
    private ShaderProgram program;

    private FloatBuffer vertices;
    private int numVertices;
    private boolean drawing;

    public void init() {
        setupShaderProgram();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void begin() {
        if (drawing) {
            throw new IllegalStateException("Renderer is already drawing!");
        }
        drawing = true;
        numVertices = 0;
    }

    public void end() {
        if (!drawing) {
            throw new IllegalStateException("Renderer isn't drawing!");
        }
        drawing = false;
        flush();
    }

    public void flush() {
        if (numVertices > 0) {
            vertices.flip();

            if (vao != null) {
                vao.bind();
            } else {
                vbo.bind(GL_ARRAY_BUFFER);
                specifyVertexAttributes();
            }
            program.use();

            vbo.bind(GL_ARRAY_BUFFER);
            vbo.uploadSubData(GL_ARRAY_BUFFER, 0, vertices);

            glDrawArrays(GL_TRIANGLES, 0, numVertices);

            vertices.clear();
            numVertices = 0;
        }
    }

    public void drawCube() {
        if (vertices.remaining() < 3 * 36) {
            flush();
        }

        vertices.put(-0.5f).put(-0.5f).put(-0.5f);
        vertices.put(-0.5f).put(-0.5f).put(0.5f);
        vertices.put(-0.5f).put(0.5f).put(0.5f);
        vertices.put(0.5f).put(0.5f).put(-0.5f);
        vertices.put(-0.5f).put(-0.5f).put(-0.5f);
        vertices.put(-0.5f).put(0.5f).put(-0.5f);

        vertices.put(0.5f).put(-0.5f).put(0.5f);
        vertices.put(-0.5f).put(-0.5f).put(-0.5f);
        vertices.put(0.5f).put(-0.5f).put(-0.5f);
        vertices.put(0.5f).put(0.5f).put(-0.5f);
        vertices.put(0.5f).put(-0.5f).put(-0.5f);
        vertices.put(-0.5f).put(-0.5f).put(-0.5f);

        vertices.put(-0.5f).put(-0.5f).put(-0.5f);
        vertices.put(-0.5f).put(0.5f).put(0.5f);
        vertices.put(-0.5f).put(0.5f).put(-0.5f);
        vertices.put(0.5f).put(-0.5f).put(0.5f);
        vertices.put(-0.5f).put(-0.5f).put(0.5f);
        vertices.put(-0.5f).put(-0.5f).put(-0.5f);

        vertices.put(-0.5f).put(0.5f).put(0.5f);
        vertices.put(-0.5f).put(-0.5f).put(0.5f);
        vertices.put(0.5f).put(-0.5f).put(0.5f);
        vertices.put(0.5f).put(0.5f).put(0.5f);
        vertices.put(0.5f).put(-0.5f).put(-0.5f);
        vertices.put(0.5f).put(0.5f).put(-0.5f);

        vertices.put(0.5f).put(-0.5f).put(-0.5f);
        vertices.put(0.5f).put(0.5f).put(0.5f);
        vertices.put(0.5f).put(-0.5f).put(0.5f);
        vertices.put(0.5f).put(0.5f).put(0.5f);
        vertices.put(0.5f).put(0.5f).put(-0.5f);
        vertices.put(-0.5f).put(0.5f).put(-0.5f);

        vertices.put(0.5f).put(0.5f).put(0.5f);
        vertices.put(-0.5f).put(0.5f).put(-0.5f);
        vertices.put(-0.5f).put(0.5f).put(0.5f);
        vertices.put(0.5f).put(0.5f).put(0.5f);
        vertices.put(-0.5f).put(0.5f).put(0.5f);
        vertices.put(0.5f).put(-0.5f).put(0.5f);

        numVertices += 36;
    }

    private void setupShaderProgram() {
        vao = new VertexArrayObject();
        vao.bind();

        vbo = new VertexBufferObject();
        vbo.bind(GL_ARRAY_BUFFER);

        vertices = BufferUtils.createFloatBuffer(4096);
        long size = vertices.capacity() * Float.BYTES;
        vbo.uploadData(GL_ARRAY_BUFFER, size, GL_DYNAMIC_DRAW);

        numVertices = 0;
        drawing = false;

        Shader vertexShader = Shader.loadShader(GL_VERTEX_SHADER, "shaders/vshader.glsl");
        Shader fragmentShader = Shader.loadShader(GL_FRAGMENT_SHADER, "shaders/fshader.glsl");
        program = new ShaderProgram();
        program.attachShader(vertexShader);
        program.attachShader(fragmentShader);
        program.link();
        program.use();
        vertexShader.delete();
        fragmentShader.delete();

        long window = GLFW.glfwGetCurrentContext();
        int width, height;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
            width = widthBuffer.get();
            height = heightBuffer.get();
        }

        specifyVertexAttributes();


        Matrix4f model = new Matrix4f().identity();
        updateModelMatrix(model);

        Matrix4f view = new Matrix4f().lookAt(0.0f, 0.0f, -4.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f);
        updateViewMatrix(view);

        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45.0f), (float) width / (float) height, 0.01f, 100.0f);
        updateProjectionMatrix(projection);

    }

    private void specifyVertexAttributes() {
        int posAttribute = program.getAttributeLocation("position");
        program.enableVertexAttribute(posAttribute);
        program.pointVertexAttribute(posAttribute, 3, 3 * Float.BYTES, 0);
    }

    public void updateModelMatrix(Matrix4f model) {
        program.setUniform(program.getUniformLocation("model"), model);
    }

    public void updateViewMatrix(Matrix4f view) {
        program.setUniform(program.getUniformLocation("view"), view);
    }

    public void updateProjectionMatrix(Matrix4f projection) {
        program.setUniform(program.getUniformLocation("projection"), projection);
    }


    public void delete() {
        MemoryUtil.memFree(vertices);
        vbo.delete();
        program.delete();
    }


}
