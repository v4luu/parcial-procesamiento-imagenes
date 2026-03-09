#include <jni.h>
#include <opencv2/opencv.hpp>

using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_parcial_MainActivity_processWithOpenCV(JNIEnv *env, jobject thiz,
        jintArray pixels, jint width, jint height, jint filterType) {

jint *arr = env->GetIntArrayElements(pixels, NULL);

// Mat desde pixels ARGB
Mat img(height, width, CV_8UC4, arr);

switch (filterType) {
case 0: // Original
break;

case 1: // Gris
{
Mat gray;
cvtColor(img, gray, COLOR_RGBA2GRAY);
cvtColor(gray, img, COLOR_GRAY2RGBA);
}
break;

case 2: // Sepia
{
Mat sepiaKernel = (Mat_<float>(4,4) <<
                                    0.393, 0.769, 0.189, 0,
        0.349, 0.686, 0.168, 0,
        0.272, 0.534, 0.131, 0,
        0,     0,     0,     1);
transform(img, img, sepiaKernel);
}
break;

case 3: // Blur
GaussianBlur(img, img, Size(7,7), 0);
break;

case 4: // Edge Detection
{
Mat gray, edges;
cvtColor(img, gray, COLOR_RGBA2GRAY);
Canny(gray, edges, 100, 200);
cvtColor(edges, img, COLOR_GRAY2RGBA);
}
break;

case 5: // Resaltar rojo sobre fondo B/N
{
Mat rgb, hsv, mask1, mask2, maskRed, gray;

// RGBA -> RGB
cvtColor(img, rgb, COLOR_RGBA2RGB);

// RGB -> HSV
cvtColor(rgb, hsv, COLOR_RGB2HSV);

// Rango rojo (OpenCV hue 0-180)
inRange(hsv, Scalar(0, 100, 50), Scalar(10, 255, 255), mask1);
inRange(hsv, Scalar(160, 100, 50), Scalar(180, 255, 255), mask2);
maskRed = mask1 | mask2; // combinar rangos rojo

// Imagen en gris
cvtColor(rgb, gray, COLOR_RGB2GRAY);
cvtColor(gray, gray, COLOR_GRAY2RGBA);

// Mezclar rojo original y gris
rgb.copyTo(gray, maskRed); // donde hay rojo se mantiene color
img = gray; // guardar resultado final en img
}
break;
} // <- Fin del switch

env->ReleaseIntArrayElements(pixels, arr, 0);
} // <- Fin de la función
