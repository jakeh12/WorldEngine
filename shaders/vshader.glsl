uniform mat4 viewProjMatrix;

void main(void) {
    gl_Position = viewProjMatrix * gl_Vertex;
}
