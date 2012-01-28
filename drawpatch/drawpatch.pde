import processing.opengl.*;

float[][] mData;

void setup() {
  size(640, 480, P3D);
  colorMode(RGB, 1); 

  mData = new float[32][24];
  randomize();
}

void keyPressed() {
  randomize();
}

void randomize() {
  int mSeed = (int)random(0, 100);
  for (int x=0; x < mData.length; x++) {
    for (int y=0; y < mData[x].length; y++) {
      mData[x][y] = pow(noise((x + mSeed) * 0.05, y * 0.05) * 2, 3);
    }
  }
}

void draw() {
  background(0);

  beginShape(QUADS);
  stroke(0);
  for (int x=1; x < mData.length - 1; x++) {
    for (int y=1; y < mData[x].length - 1; y++) {
      drawVertex(x, y);
      drawVertex(x + 1, y);
      drawVertex(x + 1, y + 1);
      drawVertex(x, y + 1);
    }
  }
  endShape();
}

void drawVertex(int x, int y) {
  final float mRatioX = x / (float)mData.length * (float)width;
  final float mRatioY = y / (float)mData[x].length * (float)height;
  PVector p0 = new PVector(mRatioX, mRatioY);
  float c0 = mData[x][y];
//  float c0 = (mData[x-1][y-1] + mData[x + 1][y + 1] ) * 0.5;
  fill(c0);
  vertex(p0.x, p0.y);
}

