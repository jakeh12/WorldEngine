#version 150 core

in vec3 position;
out vec4 vertexColor;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;


void main() {
  gl_Position = projection * view * model * vec4(position, 1.0);
  vertexColor = vec4(position.x*0.7 + 0.5, position.y*0.7 + 0.5, position.z*0.7 + 0.5, 1.0);
}