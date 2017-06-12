package peneder.ba1.ooe.fh.opencvimagemanipulationtests;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageManipulationActivity extends AppCompatActivity {

    public static final String TAG = "ImageActivity";

    private ImageView mImageView;

    private ToggleButton mSepiaModeToggleButton;
    private ToggleButton mGrayScaleToggleButton;
    private ToggleButton mBlurToggleButton;

    Mat mImageMatUnFiltered = null;
    Mat mImageMatFiltered = null;
    Mat mVignette = null;

    private boolean mSepiaModeOn = false;
    private boolean mGrayScaleModeOn = false;
    private boolean mBlurModeOn = false;
    private boolean mSobelOn = false;
    private boolean mCannyOn = false;

    private FloatingActionButton takePictureButton;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //initImageMat();
                    //scaleImage(2);
                    //flipImage(270);
                    //flipImage(180);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private void initImageMat(Bitmap imageBitMap) {
        mImageMatFiltered = new Mat();
        mImageMatUnFiltered = new Mat();
        if (imageBitMap != null) {
            Utils.bitmapToMat(imageBitMap, mImageMatFiltered);
        } else {
            Utils.bitmapToMat(BitmapFactory.decodeResource(getResources(),R.drawable.aeolian_by_wlop), mImageMatFiltered);
        }
        mImageMatUnFiltered = mImageMatFiltered.clone();

        mSepiaModeToggleButton = (ToggleButton) findViewById(R.id.sepia_mode_toggleButton);
        mGrayScaleToggleButton= (ToggleButton) findViewById(R.id.grayscale_mode_toggleButton);
        mBlurToggleButton= (ToggleButton) findViewById(R.id.blur_mode_toggleButton);
        mVignette = new Mat();
        Utils.bitmapToMat(BitmapFactory.decodeResource(getResources(),R.drawable.vignette), mVignette);

        changeResolution();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_manipulation);

        mImageView = (ImageView) findViewById(R.id.image_manipulations_activity_image_view);

        takePictureButton = (FloatingActionButton) findViewById(R.id.cameraButton);
      }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void takeImageFromCamera(View view) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            initImageMat(imageBitmap);
            changeResolution();
        }
    }



    private void setSepiaMode() {
        //Mat BGRMat = Imgcodecs.imread(ResourcesCompat.getDrawable(getResources(), R.drawable.test_image, null).toString());
        //Utils.loadResource(ImageManipulationActivity.this, R.drawable.test_image, CvType.Op);
        // convert to bitmap:
        Mat mSepiaKernel;
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);

        if (mSepiaModeOn) {
            Core.transform(mImageMatFiltered, mImageMatFiltered, mSepiaKernel);
            updateImageView(mImageMatFiltered);
            // uncheck other filter buttons
            mBlurModeOn = false;
            mGrayScaleModeOn= false;
            mGrayScaleToggleButton.setChecked(false);
            mBlurToggleButton.setChecked(false);
        } else {
            mImageMatFiltered = mImageMatUnFiltered.clone();
            updateImageView(mImageMatUnFiltered);
        }


    }

    public void displayVignette (View view) {
        Mat dest = new Mat();
        Core.subtract(mImageMatFiltered, mVignette, dest);
        updateImageView(dest);
    }

    public void rotateImage (View view) {
        flipImage(90);
    }

    public void toggleSepia (View view) {
        mSepiaModeOn = !mSepiaModeOn;
        setSepiaMode();
    }

    public void toggleBlur (View view) {
        mBlurModeOn = !mBlurModeOn;
        setBlurMode();
    }

    public void skewImage (View view) {
        deskew(45);
    }


    public void toggleGrayScale (View view) {
        mGrayScaleModeOn = !mGrayScaleModeOn;
        setGrayScaleMode();
    }

    private void setBlurMode() {
        if (mBlurModeOn) {
            //Imgproc.GaussianBlur(mImageMatUnFiltered, mImageMatFiltered, new Size(37, 37), 0);
            Imgproc.blur(mImageMatUnFiltered, mImageMatFiltered, new Size(31.0,31.0));
            updateImageView(mImageMatFiltered);
            // uncheck other filter buttons
            mSepiaModeOn = false;
            mGrayScaleModeOn= false;
            mSepiaModeToggleButton.setChecked(false);
            mGrayScaleToggleButton.setChecked(false);
        } else {
            mImageMatFiltered = mImageMatUnFiltered.clone();
            updateImageView(mImageMatUnFiltered);
        }
    }

    private void setGrayScaleMode() {
        if (mGrayScaleModeOn) {
            Imgproc.cvtColor(mImageMatUnFiltered, mImageMatFiltered, Imgproc.COLOR_BGR2GRAY);
            Imgproc.cvtColor(mImageMatFiltered, mImageMatFiltered, Imgproc.COLOR_GRAY2RGBA, 4);
            updateImageView(mImageMatFiltered);
            // uncheck other filter buttons
            mSepiaModeToggleButton.setChecked(false);
            mBlurToggleButton.setChecked(false);
            mSepiaModeOn = false;
            mBlurModeOn= false;
        } else {
            mImageMatFiltered = mImageMatUnFiltered.clone();
            updateImageView(mImageMatUnFiltered);
        }
    }

    private void flipImage (int rotationAngle) {
        if (rotationAngle == 270) {
            // Rotate clockwise 270 degrees
            Core.flip(mImageMatFiltered.t(), mImageMatFiltered, 0);
            Core.flip(mImageMatUnFiltered.t(), mImageMatUnFiltered, 0);
        } else if (rotationAngle == 180) {
            // Rotate clockwise 180 degrees
            Core.flip(mImageMatFiltered, mImageMatFiltered, -1);
            Core.flip(mImageMatUnFiltered, mImageMatUnFiltered, -1);
        } else if (rotationAngle == 90) {
            // Rotate clockwise 90 degrees
            Core.flip(mImageMatFiltered.t(), mImageMatFiltered, 1);
            Core.flip(mImageMatUnFiltered.t(), mImageMatUnFiltered, 1);
        }
        updateImageView(mImageMatFiltered);
    }

    public void erodeImage (View view) {
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_CROSS, new Size(2*5+1, 2*5+1), new Point(5, 5));

        Imgproc.erode(mImageMatUnFiltered, mImageMatFiltered, element);

        updateImageView(mImageMatFiltered);
    }

    public void sobel (View view) {
        mSobelOn = !mSobelOn;
        if (mSobelOn) {
            // init
            Mat grayImage = new Mat();
            Mat detectedEdges = new Mat();
            int scale = 1;
            int delta = 0;
            int ddepth = CvType.CV_16S;
            Mat grad_x = new Mat();
            Mat grad_y = new Mat();
            Mat abs_grad_x = new Mat();
            Mat abs_grad_y = new Mat();

            // reduce noise with a 3x3 kernel
            Imgproc.GaussianBlur(mImageMatUnFiltered, mImageMatFiltered, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);

            // convert to grayscale
            Imgproc.cvtColor(mImageMatFiltered, grayImage, Imgproc.COLOR_BGR2GRAY);

            // Gradient X
            // Imgproc.Sobel(grayImage, grad_x, ddepth, 1, 0, 3, scale,
            // this.threshold.getValue(), Core.BORDER_DEFAULT );
            Imgproc.Sobel(grayImage, grad_x, ddepth, 1, 0);
            Core.convertScaleAbs(grad_x, abs_grad_x);

            // Gradient Y
            // Imgproc.Sobel(grayImage, grad_y, ddepth, 0, 1, 3, scale,
            // this.threshold.getValue(), Core.BORDER_DEFAULT );
            Imgproc.Sobel(grayImage, grad_y, ddepth, 0, 1);
            Core.convertScaleAbs(grad_y, abs_grad_y);

            // Total Gradient (approximate)
            Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, detectedEdges);
            // Core.addWeighted(grad_x, 0.5, grad_y, 0.5, 0, detectedEdges);


            updateImageView(mImageMatFiltered);
        } else {
            mImageMatUnFiltered.copyTo(mImageMatFiltered);
            updateImageView(mImageMatUnFiltered);
        }
    }

    public void cannyEdgeDetection (View view) {
        mCannyOn = !mCannyOn;
        if (mCannyOn) {
            Imgproc.cvtColor(mImageMatUnFiltered, mImageMatFiltered, Imgproc.COLOR_RGB2GRAY, 4);
            Imgproc.Canny(mImageMatFiltered, mImageMatFiltered, 80, 100);
            updateImageView(mImageMatFiltered);
        }
    }

    private void scaleImage (double zoomfactor) {
        Size sizeRgba = mImageMatFiltered.size();
        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        Mat zoomCorner = mImageMatFiltered.submat(rows / 4, (rows *3) / 4 , cols/4, (cols * 3) / 4);
        Imgproc.resize(zoomCorner, mImageMatFiltered, zoomCorner.size());
        //Imgproc.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);
        zoomCorner.release();
        mImageMatFiltered.copyTo(mImageMatUnFiltered);
        updateImageView(mImageMatFiltered);
    }

    private void changeResolution () {
        Size size = new Size (1000,1000);//the dst image size,e.g.100x100

        Imgproc.resize(mImageMatFiltered,mImageMatFiltered,size);//resize image
        Imgproc.resize(mVignette,mVignette,size);//resize image
        mImageMatUnFiltered = mImageMatFiltered.clone();
        updateImageView(mImageMatFiltered);
    }

    /**
     * Image is rotated - cropped-to-fit dst Mat.
     *
     */
    private void deskew(double angle) {
        Point center = new Point(mImageMatFiltered.width() / 2, mImageMatFiltered.height() / 2);
        Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        Size size = new Size(mImageMatFiltered.width(), mImageMatFiltered.height());

        Imgproc.warpAffine(mImageMatFiltered, mImageMatFiltered, rotImage, size, Imgproc.INTER_LINEAR
                + Imgproc.CV_WARP_FILL_OUTLIERS);

        updateImageView(mImageMatFiltered);

    }

    private void updateImageView(Mat imageMat) {
        Bitmap bm = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(imageMat, bm);

        // find the imageview and draw it!
        mImageView.setImageBitmap(bm);
    }

}
