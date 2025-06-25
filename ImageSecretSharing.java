import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class ImageSecretSharing {

    /**
     * Main method that demonstrates the functionality:
     * 1. Generates secret shares from an input image
     * 2. Reconstructs the original image from shares
     * 3. Demonstrates homomorphic downscaling properties
     */

    public static void main(String[] args) {
        try {
            String inputImage = "C:\\Users\\lbans\\eclipse-workspace\\HW4_426\\src\\Screenshot 2024-06-09 025254.bmp";
            String outputPrefix = "output";

            File inputFile = new File(inputImage);
            if (!inputFile.exists()) {
                throw new IOException("Input image not found: " + inputImage);
            }

            // Problem 1: Generate and reconstruct shares  Shamir's Secret Sharing
            generateImageShares(inputImage, outputPrefix);
            List<String> sharesToReconstruct = List.of(
                    outputPrefix + "_share1.bmp",
                    outputPrefix + "_share2.bmp"
            );
            reconstructImageFromShares(outputPrefix, sharesToReconstruct);

            // Problem 2: Homomorphic downscaling
            homomorphicDownscalingDemo(inputImage);

        } catch (IOException e) {
            System.err.println(" Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Problem 1: Generate shares
    /**
     * Generates n secret shares from an input image using Shamir's Secret Sharing.
     *
     * @param inputPath Path to the input image
     * @param outputPrefix Prefix for output share files
     * @throws IOException If there's an error reading/writing image files
     */
    public static void generateImageShares(String inputPath, String outputPrefix) throws IOException {
        BufferedImage img = ImageIO.read(new File(inputPath));
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage s1 = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage s2 = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage s3 = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

        // Use a prime < 256 so share values fit in one byte
        ShamirSecretSharing sss = new ShamirSecretSharing(2, 3, BigInteger.valueOf(251));

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int px = img.getRGB(x, y) & 0xFF;
                List<ShamirSecretSharing.Point> shares = sss.generateShares(BigInteger.valueOf(px));
                for (int i = 0; i < 3; i++) {
                    int v = shares.get(i).y.intValue();
                    int rgb = (v << 16) | (v << 8) | v;
                    if (i == 0) s1.setRGB(x, y, rgb);
                    else if (i == 1) s2.setRGB(x, y, rgb);
                    else s3.setRGB(x, y, rgb);
                }
            }
        }

        ImageIO.write(s1, "bmp", new File(outputPrefix + "_share1.bmp"));
        ImageIO.write(s2, "bmp", new File(outputPrefix + "_share2.bmp"));
        ImageIO.write(s3, "bmp", new File(outputPrefix + "_share3.bmp"));
        System.out.println(" Generated shares: " +
                outputPrefix + "_share1.bmp, " +
                outputPrefix + "_share2.bmp, " +
                outputPrefix + "_share3.bmp");
    }

    //  Problem 1: Reconstruct
    /**
     * Reconstructs the original image from a subset of shares.
     *
     * @param outputPrefix Prefix for output reconstructed image
     * @param sharePaths List of paths to share images to use for reconstruction
     * @throws IOException If there's an error reading share images or writing output
     */
    public static void reconstructImageFromShares(String outputPrefix, List<String> sharePaths) throws IOException {
        List<BufferedImage> imgs = new ArrayList<>();
        for (String path : sharePaths) {
            File f = new File(path);
            if (!f.exists()) throw new IOException("Missing share: " + path);
            imgs.add(ImageIO.read(f));
        }
        int w = imgs.get(0).getWidth(), h = imgs.get(0).getHeight();
        BufferedImage rec = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        ShamirSecretSharing sss = new ShamirSecretSharing(2, 3, BigInteger.valueOf(251));

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                List<ShamirSecretSharing.Point> pts = new ArrayList<>();
                for (int i = 0; i < imgs.size(); i++) {
                    int v = imgs.get(i).getRGB(x, y) & 0xFF;
                    pts.add(new ShamirSecretSharing.Point(
                            BigInteger.valueOf(i + 1), BigInteger.valueOf(v)));
                }
                int secret = sss.reconstructSecret(pts).intValue();
                int rgb = (secret << 16) | (secret << 8) | secret;
                rec.setRGB(x, y, rgb);
            }
        }

        String out = outputPrefix + "_reconstructed.bmp";
        ImageIO.write(rec, "bmp", new File(out));
        System.out.println(" Reconstructed image saved: " + out);
    }

    // Problem 2: Homomorphic downscaling

    /**
     * Demonstrates homomorphic downscaling properties of the secret sharing scheme.
     *
     * @param inputPath Path to the input image
     * @throws IOException If there's an error reading/writing image files
     */
    public static void homomorphicDownscalingDemo(String inputPath) throws IOException {
        BufferedImage orig = ImageIO.read(new File(inputPath));
        BufferedImage Io   = downscale(orig);
        String downOrig = "original_downscaled.bmp";
        ImageIO.write(Io, "bmp", new File(downOrig));
        System.out.println(" Saved downscaled original: " + downOrig);

        // generate shares of full-res
        generateImageShares(inputPath, "share");

        BufferedImage S1 = downscale(ImageIO.read(new File("share_share1.bmp")));
        BufferedImage S2 = downscale(ImageIO.read(new File("share_share2.bmp")));
        BufferedImage S3 = downscale(ImageIO.read(new File("share_share3.bmp")));

        // save all three downscaled shares
        String ds1 = "share_downscaled1.bmp";
        String ds2 = "share_downscaled2.bmp";
        String ds3 = "share_downscaled3.bmp";
        ImageIO.write(S1, "bmp", new File(ds1));
        System.out.println(" Saved downscaled share: " + ds1);
        ImageIO.write(S2, "bmp", new File(ds2));
        System.out.println(" Saved downscaled share: " + ds2);
        ImageIO.write(S3, "bmp", new File(ds3));
        System.out.println("Saved downscaled share: " + ds3);

        // pick any two to reconstruct
        List<ShamirSecretSharing.Point> pts = new ArrayList<>();
        int w = S1.getWidth(), h = S1.getHeight();
        BufferedImage rec = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        ShamirSecretSharing sss = new ShamirSecretSharing(2, 3, BigInteger.valueOf(251));

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pts.clear();
                pts.add(new ShamirSecretSharing.Point(
                        BigInteger.ONE, BigInteger.valueOf(S1.getRGB(x, y) & 0xFF)));
                pts.add(new ShamirSecretSharing.Point(
                        BigInteger.valueOf(2), BigInteger.valueOf(S2.getRGB(x, y) & 0xFF)));

                int s = sss.reconstructSecret(pts).intValue();
                s = Math.floorMod(s, 251);  // Clamp reconstructed pixel to field
                rec.setRGB(x, y, (s << 16) | (s << 8) | s);
            }
        }


        // Save reconstructed downscaled image
        String dsRec = "reconstructed_downscaled.bmp";
        ImageIO.write(rec, "bmp", new File(dsRec));
        System.out.println("Reconstructed downscaled image: " + dsRec);
        // Calculate and print Mean Absolute Error between direct downscale and reconstructed downscale
        double mae = calculateMAE(Io, rec);
        System.out.printf("MAE between %s and %s = %.4f%n", downOrig, dsRec, mae);
    }

    /**
     * Downscales an image by averaging 2x2 pixel blocks.
     *
     * @param img Input image to downscale
     * @return Downscaled image (half width and height)
     */
    private static BufferedImage downscale(BufferedImage img) {
        int w = img.getWidth() / 2, h = img.getHeight() / 2;
        BufferedImage ds = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = img.getRGB(x*2, y*2) & 0xFF;
                int b = img.getRGB(x*2+1, y*2) & 0xFF;
                int c = img.getRGB(x*2, y*2+1) & 0xFF;
                int d = img.getRGB(x*2+1, y*2+1) & 0xFF;

                // Average using float and clamp to field (0–250)
                int avg = Math.round((a + b + c + d) / 4.0f) % 251;

                ds.setRGB(x, y, (avg << 16) | (avg << 8) | avg);
            }
        }
        return ds;
    }

    /**
     * Calculates Mean Absolute Error between two images.
     *
     * @param a First image
     * @param b Second image
     * @return MAE between the two images
     * @throws IllegalArgumentException if images have different dimensions
     */
    private static double calculateMAE(BufferedImage a, BufferedImage b) {
        int w = a.getWidth(), h = a.getHeight();
        if (w != b.getWidth() || h != b.getHeight()) {
            throw new IllegalArgumentException("Size mismatch");
        }
        double sum = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                sum += Math.abs((a.getRGB(x, y) & 0xFF) - (b.getRGB(x, y) & 0xFF));
            }
        }
        return sum / (w * h);
    }

    // Nested ShamirSecretSharing class

    /**
     * Implementation of Shamir's Secret Sharing scheme.
     */
    static class ShamirSecretSharing {
        private final int k, n;
        private final BigInteger p;
        private final SecureRandom rnd;

        /**
         * Constructor for Shamir's Secret Sharing scheme.
         *
         * @param k Minimum number of shares needed for reconstruction
         * @param n Total number of shares to generate
         * @param prime Prime modulus for finite field arithmetic
         */
        ShamirSecretSharing(int k, int n, BigInteger prime) {
            this.k = k;
            this.n = n;
            this.p = prime;
            this.rnd = new SecureRandom();
        }

        /**
         * Generates n shares for a given secret using polynomial interpolation.
         *
         * @param secret The secret to share
         * @return List of shares (points on the polynomial)
         */
        List<Point> generateShares(BigInteger secret) {
            // Generate random coefficients for the polynomial
            List<BigInteger> coef = new ArrayList<>(k);
            coef.add(secret);
            for (int i = 1; i < k; i++) {
                coef.add(new BigInteger(p.bitLength() - 1, rnd).mod(p));
            }

            // Evaluate polynomial at n points (x=1 to x=n)
            List<Point> out = new ArrayList<>(n);
            for (int x = 1; x <= n; x++) {
                BigInteger X = BigInteger.valueOf(x);
                BigInteger Y = BigInteger.ZERO;

                // Evaluate polynomial: Y = coef[0] + coef[1]*x + coef[2]*x^2 + ... + coef[k-1]*x^(k-1)
                for (int i = 0; i < coef.size(); i++) {
                    Y = Y.add(coef.get(i).multiply(X.pow(i))).mod(p);
                }
                // Add the contribution of this share
                out.add(new Point(X, Y));
            }
            return out;
        }


        /**
         * Reconstructs the secret from a subset of shares using Lagrange interpolation.
         *
         * @param shares List of shares to use for reconstruction
         * @return Reconstructed secret
         */
        BigInteger reconstructSecret(List<Point> shares) {
            BigInteger s = BigInteger.ZERO;

            // Lagrange interpolation
            for (int i = 0; i < shares.size(); i++) {
                Point si = shares.get(i);
                BigInteger num = BigInteger.ONE;
                BigInteger den = BigInteger.ONE;
                // Calculate Lagrange basis polynomial
                for (int j = 0; j < shares.size(); j++) {
                    if (i != j) {
                        Point sj = shares.get(j);
                        num = num.multiply(sj.x.negate()).mod(p);
                        den = den.multiply(si.x.subtract(sj.x)).mod(p);
                    }
                }
                s = s.add(si.y.multiply(num.multiply(den.modInverse(p)))).mod(p);
            }
            return s;
        }

        /**
         * Represents a point (x,y) on the polynomial.
         */

        static class Point {
            final BigInteger x, y;
            Point(BigInteger x, BigInteger y) {
                this.x = x;
                this.y = y;
            }
        }
    }
}
