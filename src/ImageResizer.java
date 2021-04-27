import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageResizer {

    private static final String BASE_IMAGE_PATH = "src/resources/puppy.jpg";

    public static void main(String[] args) {
        File baseImage = new File(BASE_IMAGE_PATH);
        ProcessingDimension[] baseProcessingDimensions = new ProcessingDimension[]{
                new ProcessingDimension("GIF", 1.5),
                new ProcessingDimension("JPEG", 0.5),
                new ProcessingDimension("JPEG", 0.25)
        };

        // Measure and execute image processing sequentially
        long sequentialStartTime = System.currentTimeMillis();
        for (ProcessingDimension baseProcessingDimension : baseProcessingDimensions) {
            ImageProcessor.calculateImageDimensions(
                    baseImage,
                    baseProcessingDimension.getExtension(),
                    baseProcessingDimension.getScale()
            );
        }
        long sequentialEndTime = System.currentTimeMillis();

        // Measure and execute image processing parallelism
        long parallelStartTime = System.currentTimeMillis();
        for (ProcessingDimension baseProcessingDimension : baseProcessingDimensions) {
            new ImageParallelismProcessing(baseProcessingDimension, baseImage).start();
        }
        long parallelEndTime = System.currentTimeMillis();

        System.out.println("Sequential execution time: " + (sequentialEndTime - sequentialStartTime) + " millis");
        System.out.println("Parallel execution time: " + (parallelEndTime - parallelStartTime) + " millis");
    }
}

class ProcessingDimension {

    private final String extension;
    private final double scale;

    public ProcessingDimension(String extension, double scale) {
        this.extension = extension;
        this.scale = scale;
    }

    public String getExtension() {
        return extension;
    }

    public double getScale() {
        return scale;
    }
}

class ImageParallelismProcessing extends Thread {

    private final ProcessingDimension dimension;
    private final File image;

    public ImageParallelismProcessing(ProcessingDimension dimension, File image) {
        this.dimension = dimension;
        this.image = image;
    }

    public void run() {
        ImageProcessor.calculateImageDimensions(image, dimension.getExtension(), dimension.getScale());
    }
}

class ImageProcessor {

    protected static void calculateImageDimensions(File inputImage, String extension, double percent) {
        try {
            BufferedImage sourceImage = ImageIO.read(inputImage);

            int scaledWidth = (int) (sourceImage.getWidth() * percent);
            int scaledHeight = (int) (sourceImage.getHeight() * percent);
            resize(inputImage, sourceImage, scaledWidth, scaledHeight, extension);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void resize(File inputImage, BufferedImage sourceImage, int width, int height, String extension) {
        try {
            int type = sourceImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : sourceImage.getType();

            BufferedImage resizedImage = new BufferedImage(width, height, type);
            Graphics2D graphics = resizedImage.createGraphics();
            graphics.drawImage(sourceImage, 0, 0, width, height, null);
            graphics.dispose();

            String outputImageTitleSuffix = "_" + width + "x" + height + "." + extension;
            String outputImage = inputImage
                    .getAbsolutePath()
                    .substring(0, inputImage.getAbsolutePath().lastIndexOf(".")) + outputImageTitleSuffix;
            ImageIO.write(resizedImage, extension, new File(outputImage));

            System.out.println("File created at " + outputImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
