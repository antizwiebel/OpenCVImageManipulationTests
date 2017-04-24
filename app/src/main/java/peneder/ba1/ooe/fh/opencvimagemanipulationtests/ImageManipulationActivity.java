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

    Mat mImageMatUnFiltered = null;
    Mat mImageMatFiltered = null;
    Mat mVignette = null;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    initImageMat();
                    //setSepiaMode();
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
        Utils.bitmapToMat(BitmapFactory.decodeResource(getResources(),R.drawable.test_image), mImageMatFiltered);
        mImageMatUnFiltered = mImageMatFiltered.clone();
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

    private void setSepiaMode(boolean checked) {
        //Mat BGRMat = Imgcodecs.imread(ResourcesCompat.getDrawable(getResources(), R.drawable.test_image, null).toString());
        //Utils.loadResource(ImageManipulationActivity.this, R.drawable.test_image, CvType.Op);
        // convert to bitmap:
        Mat mSepiaKernel;
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);

        if (checked) {
            Core.transform(mImageMatFiltered, mImageMatFiltered, mSepiaKernel);
            updateImageView(mImageMatFiltered);
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
        ToggleButton button = (ToggleButton) view;
        setSepiaMode (button.isChecked());
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
