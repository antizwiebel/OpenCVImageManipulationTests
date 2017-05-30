package peneder.ba1.ooe.fh.opencvimagemanipulationtests;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import static org.opencv.imgcodecs.Imgcodecs.imread;

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

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    initImageMat();
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

    private void initImageMat() {
        mImageMatFiltered = new Mat();
        mImageMatUnFiltered = new Mat();
        //Bitmap bitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
        Utils.bitmapToMat(BitmapFactory.decodeResource(getResources(),R.drawable.aeolian_by_wlop), mImageMatFiltered);
        mImageMatUnFiltered = mImageMatFiltered.clone();

        mSepiaModeToggleButton = (ToggleButton) findViewById(R.id.sepia_mode_toggleButton);
        mGrayScaleToggleButton= (ToggleButton) findViewById(R.id.grayscale_mode_toggleButton);
        mBlurToggleButton= (ToggleButton) findViewById(R.id.blur_mode_toggleButton);
        //mVignette = new Mat();
        //Utils.bitmapToMat(BitmapFactory.decodeResource(getResources(),R.drawable.vignette), mVignette);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_manipulation);

        mImageView = (ImageView) findViewById(R.id.image_manipulations_activity_image_view);
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
            Imgproc.Sobel(mImageMatFiltered, mImageMatFiltered, CvType.CV_8U, 1,1);
            updateImageView(mImageMatFiltered);
        } else {
            mImageMatFiltered = mImageMatUnFiltered.clone();
            updateImageView(mImageMatUnFiltered);
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
