public class Regression {

	private double[] xPoints;
	private double[] yPoints;
	private RegressionType type;
	private int polynomialOrder;

	private double[] coefficients;
	private double correlation;
	private double[] residuals;

	public Regression(double[] xPoints, double[] yPoints, RegressionType type) {
		this.xPoints = xPoints;
		this.yPoints = yPoints;
		this.type = type;
	}

	public Regression(double[] xPoints, double[] yPoints, RegressionType type, int polynomialOrder) {
		this.xPoints = xPoints;
		this.yPoints = yPoints;
		this.type = type;
		this.polynomialOrder = polynomialOrder;
	}

	public double[] calculate() {
		switch (type) {
			case LINEAR: {
				coefficients = new double[2];
				double[] newCoefficients = getLinearCoefficients(xPoints, yPoints);
				System.arraycopy(newCoefficients, 0, coefficients, 0, 2);
				correlation = newCoefficients[2];
				break;
			}
			case EXPONENTIAL: {
				coefficients = new double[2];
				double[] newCoefficients = getLinearCoefficients(xPoints, logTransform(yPoints));
				System.arraycopy(newCoefficients, 0, coefficients, 0, 2);
				correlation = newCoefficients[2];
				coefficients[0] = Math.exp(coefficients[0]);
				coefficients[1] = Math.exp(coefficients[1]);
				break;
			}
			case POWER: {
				coefficients = new double[2];
				double[] newCoefficients = getLinearCoefficients(logTransform(xPoints), logTransform(yPoints));
				System.arraycopy(newCoefficients, 0, coefficients, 0, 2);
				correlation = newCoefficients[2];
				coefficients[0] = Math.exp(coefficients[0]);
				break;
			}
			case LOGARITHMIC: {
				coefficients = new double[2];
				double[] newCoefficients = getLinearCoefficients(logTransform(xPoints), yPoints);
				System.arraycopy(newCoefficients, 0, coefficients, 0, 2);
				correlation = newCoefficients[2];
				break;
			}
			case POLYNOMIAL: {
				// TODO
				break;
			}
			default:
				break;
		}

		return coefficients;
	}

	protected static double[] getLinearCoefficients(double[] xPoints, double[] yPoints) {
		if (xPoints.length != yPoints.length || xPoints.length < 2 || yPoints.length < 2) throw new IllegalArgumentException();

		double meanX = sumOf(xPoints) / xPoints.length;
		double meanY = sumOf(yPoints) / yPoints.length;

		double stdDevX = 0;
		double stdDevY = 0;
		for (int i = 0; i < xPoints.length; i++) {
			stdDevX += (meanX - xPoints[i]) * (meanX - xPoints[i]);
			stdDevY += (meanY - yPoints[i]) * (meanY - yPoints[i]);
		}

		stdDevX = Math.sqrt(stdDevX / (xPoints.length - 1));
		stdDevY = Math.sqrt(stdDevY / (yPoints.length - 1));

		double multSum = 0;
		for (int i = 0; i < xPoints.length; i++) {
			multSum += ((xPoints[i] - meanX) / stdDevX) * ((yPoints[i] - meanY) / stdDevY);
		}

		double r = multSum / (xPoints.length - 1);
		double b = r * stdDevY / stdDevX;

		return new double[] {meanY - b * meanX, b, r};
	}
	
	protected static final double sumOf(double... values) {
		double sum = 0;
		for (double val : values)
			sum += val;

		return sum;
	}

	protected static final double[] logTransform(double... vals) {
		double[] ret = new double[vals.length];

		for (int i = 0; i < vals.length; i++) {
			ret[i] = Math.log(vals[i]);
		}

		return ret;
	}

	public double[] getCoefficients() {
		return coefficients;
	}

	public double getCorrelation() {
		return correlation;
	}

	public double[] getResiduals() {
		return residuals;
	}

	public static enum RegressionType {
		LINEAR,
		EXPONENTIAL,
		POWER,
		LOGARITHMIC,
		POLYNOMIAL;
		// SINUSOIDAL
	}

}
