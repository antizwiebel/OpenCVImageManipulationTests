package peneder.ba1.ooe.fh.opencvimagemanipulationtests;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageManipulationActivity extends AppCompatActivity {

    public static final String TAG = "ImageActivity";

    private ImageView mImageView;

    private ToggleButton mSepiaModeToggleButton;
    private ToggleButton mGrayScaleToggleButton;
    private ToggleButton mBlurToggleButton;
    private MenuItem mSaveImage;

    Mat mImageMatUnFiltered = null;
    Mat mImageMatFiltered = null;
    Mat mVignette = null;

    private boolean mSepiaModeOn = false;
    private boolean mGrayScaleModeOn = false;
    private boolean mBlurModeOn = false;
    private boolean mSobelOn = false;
    private boolean mCannyOn = false;
    private boolean mRemoveBackground = false;

    private FloatingActionButton takePictureButton;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private boolean pictureTaken = false;

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

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //init image mats
                    if (!pictureTaken)
                        initImageMat(null);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mSaveImage  = menu.add("Save image");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mSaveImage) {
            saveImage();
        }
        return true;
    }

    private void initImageMat(Bitmap imageBitMap) {
        mImageMatFiltered = new Mat();
        mImageMatUnFiltered = new Mat();

        Bitmap standardImageBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.lenna);

        if (imageBitMap != null) {
            Utils.bitmapToMat(imageBitMap, mImageMatFiltered);
        } else {
            Utils.bitmapToMat(standardImageBitmap, mImageMatFiltered);
        }
        mImageMatUnFiltered = mImageMatFiltered.clone();

        mSepiaModeToggleButton = (ToggleButton) findViewById(R.id.sepia_mode_toggleButton);
        mGrayScaleToggleButton= (ToggleButton) findViewById(R.id.grayscale_mode_toggleButton);
        mBlurToggleButton= (ToggleButton) findViewById(R.id.blur_mode_toggleButton);
        mVignette = new Mat();
        Utils.bitmapToMat(BitmapFactory.decodeResource(getResources(),R.drawable.vignette), mVignette);

        changeResolution();
    }


    public void takeImageFromCamera(View view) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            pictureTaken = true;
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

            detectedEdges.copyTo(mImageMatFiltered);
            updateImageView(mImageMatFiltered);
        } else {
            mImageMatUnFiltered.copyTo(mImageMatFiltered);
            updateImageView(mImageMatUnFiltered);
        }
    }

    public void cannyEdgeDetection (View view) {
        mCannyOn = !mCannyOn;
        if (mCannyOn) {
            // convert to grayscale
            Imgproc.cvtColor(mImageMatUnFiltered, mImageMatFiltered, Imgproc.COLOR_BGR2GRAY);

            // reduce noise with a 3x3 kernel
            Imgproc.blur(mImageMatFiltered, mImageMatFiltered, new Size(3, 3));

            // canny detector, upper threshold of 80 and a lower one of 90
            Imgproc.Canny(mImageMatFiltered, mImageMatFiltered, 80, 90);
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
        //get device screen width and height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;


        Size size = new Size (width,height-580);//the dst image size,e.g.100x100

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

    private void saveImage() {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            Bitmap image = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            Toast.makeText(getApplicationContext(), "Image saved successfully !", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Pictures/OpenCVImageManipulationTest");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public void removeBackground (View view) {
        mRemoveBackground = !mRemoveBackground;
        if (mRemoveBackground) {
            mImageMatFiltered = doBackgroundRemoval(mImageMatUnFiltered);
            updateImageView(mImageMatFiltered);
        } else {
            mImageMatUnFiltered.copyTo(mImageMatFiltered);
            updateImageView(mImageMatUnFiltered);
        }
    }

    /**
     * Perform the operations needed for removing a uniform background
     *
     * @param frame
     *            the current frame
     * @return an image with only foreground objects
     */
    private Mat doBackgroundRemoval(Mat frame)
    {
        // init
        Mat hsvImg = new Mat();
        List<Mat> hsvPlanes = new ArrayList<>();
        Mat thresholdImg = new Mat();

        int thresh_type = Imgproc.THRESH_BINARY_INV;


        // threshold the image with the average hue value
        hsvImg.create(frame.size(), CvType.CV_8U);
        Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2HSV);
        Core.split(hsvImg, hsvPlanes);

        // get the average hue value of the image
        double threshValue = this.getHistAverage(hsvImg, hsvPlanes.get(0));

        Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 179.0, thresh_type);

        Imgproc.blur(thresholdImg, thresholdImg, new Size(5, 5));

        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 3);

        Imgproc.threshold(thresholdImg, thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY);

        // create the new image
        Mat foreground = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        frame.copyTo(foreground, thresholdImg);

        return foreground;
    }

    /**
     * Get the average hue value of the image starting from its Hue channel
     * histogram
     *
     * @param hsvImg
     *            the current frame in HSV
     * @param hueValues
     *            the Hue component of the current frame
     * @return the average Hue value
     */
    private double getHistAverage(Mat hsvImg, Mat hueValues)
    {
        // init
        double average = 0.0;
        Mat hist_hue = new Mat();
        // 0-180: range of Hue values
        MatOfInt histSize = new MatOfInt(180);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        // compute the histogram
        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));

        // get the average Hue value of the image
        // (sum(bin(h)*h))/(image-height*image-width)
        // -----------------
        // equivalent to get the hue of each pixel in the image, add them, and
        // divide for the image size (height and width)
        for (int h = 0; h < 180; h++)
        {
            // for each bin, get its value and multiply it for the corresponding
            // hue
            average += (hist_hue.get(h, 0)[0] * h);
        }

        // return the average hue of the image
        return average = average / hsvImg.size().height / hsvImg.size().width;
    }

    private void updateImageView(Mat imageMat) {
        Bitmap bm = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(imageMat, bm);

        // find the imageview and draw it!
        mImageView.setImageBitmap(bm);
    }

}
