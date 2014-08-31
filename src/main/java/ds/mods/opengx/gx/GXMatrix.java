package ds.mods.opengx.gx;

import java.nio.FloatBuffer;

import com.google.common.io.ByteArrayDataInput;

/**
 * Holds a 4x4 float matrix.
 *
 * @author foo
 */
public class GXMatrix {
	private static final long serialVersionUID = 1L;

	public float[] m = new float[4*4];

	/**
	 * Construct a new matrix, initialized to the identity.
	 */
	public GXMatrix() {
		super();
		setIdentity();
	}

	public GXMatrix(final GXMatrix src) {
		super();
		load(src);
	}

	public GXMatrix(ByteArrayDataInput fifo) {
		for(int i = 0; i < 16; i++)
			m[i] = fifo.readFloat();
	}

	/**
	 * Returns a string representation of this matrix
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < 4; i++)
			buf.append(m[4*0 + i]).append(' ').append(m[4*1 + i]).append(' ').append(m[4*2 + i]).append(' ').append(m[4*3 + i]).append('\n');
		return buf.toString();
	}

	/**
	 * Set this matrix to be the identity matrix.
	 * @return this
	 */
	public GXMatrix setIdentity() {
		return setIdentity(this);
	}

	/**
	 * Set the given matrix to be the identity matrix.
	 * @param m The matrix to set to the identity
	 * @return m
	 */
	public static GXMatrix setIdentity(GXMatrix m) {
		for(int i = 0; i < 16; i++)
			m.m[i] = 0.0f;
		for(int i = 0; i < 16; i += 5)
			m.m[i] = 1.0f;

		return m;
	}

	/**
	 * Set this matrix to 0.
	 * @return this
	 */
	public GXMatrix setZero() {
		return setZero(this);
	}

	/**
	 * Set the given matrix to 0.
	 * @param m The matrix to set to 0
	 * @return m
	 */
	public static GXMatrix setZero(GXMatrix m) {
		for(int i = 0; i < 16; i++)
			m.m[i] = 0.0f;

		return m;
	}

	/**
	 * Load from another GXMatrix
	 * @param src The source matrix
	 * @return this
	 */
	public GXMatrix load(GXMatrix src) {
		return load(src, this);
	}

	/**
	 * Copy the source matrix to the destination matrix
	 * @param src The source matrix
	 * @param dest The destination matrix, or null of a new one is to be created
	 * @return The copied matrix
	 */
	public static GXMatrix load(GXMatrix src, GXMatrix dest) {
		if (dest == null)
			dest = new GXMatrix();

		for(int i = 0; i < 16; i++)
			dest.m[i] = src.m[i];

		return dest;
	}

	/**
	 * Load from a float buffer. The buffer stores the matrix in column major
	 * (OpenGL) order.
	 *
	 * @param buf A float buffer to read from
	 * @return this
	 */
	public GXMatrix load(FloatBuffer buf) {

		for(int i = 0; i < 16; i++)
			m[i] = buf.get();

		return this;
	}

	/**
	 * Load from a float buffer. The buffer stores the matrix in row major
	 * (maths) order.
	 *
	 * @param buf A float buffer to read from
	 * @return this
	 */
	public GXMatrix loadTranspose(FloatBuffer buf) {

		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; i++)
				m[4*j + i] = buf.get();

		return this;
	}

	/**
	 * Store this matrix in a float buffer. The matrix is stored in column
	 * major (openGL) order.
	 * @param buf The buffer to store this matrix in
	 */
	public GXMatrix store(FloatBuffer buf) {
		for(int i = 0; i < 16; i++)
			buf.put(m[i]);

		return this;
	}

	/**
	 * Store this matrix in a float buffer. The matrix is stored in row
	 * major (maths) order.
	 * @param buf The buffer to store this matrix in
	 */
	public GXMatrix storeTranspose(FloatBuffer buf) {
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				buf.put(m[4*j + i]);

		return this;
	}

	/**
	 * Store the rotation portion of this matrix in a float buffer. The matrix is stored in column
	 * major (openGL) order.
	 * @param buf The buffer to store this matrix in
	 */
	public GXMatrix store3f(FloatBuffer buf) {
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				buf.put(m[4*i + j]);

		return this;
	}

	/**
	 * Add two matrices together and place the result in a third matrix.
	 * @param left The left source matrix
	 * @param right The right source matrix
	 * @param dest The destination matrix, or null if a new one is to be created
	 * @return the destination matrix
	 */
	public static GXMatrix add(GXMatrix left, GXMatrix right, GXMatrix dest) {
		if (dest == null)
			dest = new GXMatrix();

		for(int i = 0; i < 16; i++)
			dest.m[i] = left.m[i] + right.m[i];

		return dest;
	}

	/**
	 * Subtract the right matrix from the left and place the result in a third matrix.
	 * @param left The left source matrix
	 * @param right The right source matrix
	 * @param dest The destination matrix, or null if a new one is to be created
	 * @return the destination matrix
	 */
	public static GXMatrix sub(GXMatrix left, GXMatrix right, GXMatrix dest) {
		if (dest == null)
			dest = new GXMatrix();

		for(int i = 0; i < 16; i++)
			dest.m[i] = left.m[i] - right.m[i];

		return dest;
	}

	/**
	 * Multiply the right matrix by the left and place the result in a third matrix.
	 * @param left The left source matrix
	 * @param right The right source matrix
	 * @param dest The destination matrix, or null if a new one is to be created
	 * @return the destination matrix
	 */
	public static GXMatrix mul(GXMatrix left, GXMatrix right, GXMatrix dest) {
		if (dest == null)
			dest = new GXMatrix();

		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				dest.m[4*i + j] = left.m[4*0 + j] * right.m[4*i + 0] + left.m[4*1 + j] * right.m[4*i + 1] + left.m[4*2 + j] * right.m[4*i + 2] + left.m[4*3 + j] * right.m[4*i + 3];

		return dest;
	}

	/**
	 * Transform a Vector by a matrix and return the result in a destination
	 * vector.
	 * @param left The left matrix
	 * @param right The right vector
	 * @param dest The destination vector, or null if a new one is to be created
	 * @return the destination vector
	 */
	public static float[] transform(GXMatrix left, float[] right) {

		float[] vec = new float[4];

		for(int i = 0; i < 4; i++)
			vec[i] = left.m[4*0 + i] * right[0] + left.m[4*1 + i] * right[1] + left.m[4*2 + i] * right[2] + left.m[4*3 + i] * right[3];

		return vec;
	}

	/**
	 * Transpose this matrix
	 * @return this
	 */
	public GXMatrix transpose() {
		return transpose(this);
	}

	/**
	 * Transpose this matrix and place the result in another matrix
	 * @param dest The destination matrix or null if a new matrix is to be created
	 * @return the transposed matrix
	 */
	public GXMatrix transpose(GXMatrix dest) {
		return transpose(this, dest);
	}

	/**
	 * Transpose the source matrix and place the result in the destination matrix
	 * @param src The source matrix
	 * @param dest The destination matrix or null if a new matrix is to be created
	 * @return the transposed matrix
	 */
	public static GXMatrix transpose(GXMatrix src, GXMatrix dest) {
		if (dest == null)
			dest = new GXMatrix();

		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				dest.m[4*j + i] = src.m[4*i + j];

		return dest;
	}

	/**
	 * @return the determinant of the matrix
	 */
	public float determinant() {
		float f =
				m[4*0 + 0]
						* ((m[4*1 + 1] * m[4*2 + 2] * m[4*3 + 3] + m[4*1 + 2] * m[4*2 + 3] * m[4*3 + 1] + m[4*1 + 3] * m[4*2 + 1] * m[4*3 + 2])
								- m[4*1 + 3] * m[4*2 + 2] * m[4*3 + 1]
										- m[4*1 + 1] * m[4*2 + 3] * m[4*3 + 2]
												- m[4*1 + 2] * m[4*2 + 1] * m[4*3 + 3]);
		f -= m[4*0 + 1]
				* ((m[4*1 + 0] * m[4*2 + 2] * m[4*3 + 3] + m[4*1 + 2] * m[4*2 + 3] * m[4*3 + 0] + m[4*1 + 3] * m[4*2 + 0] * m[4*3 + 2])
						- m[4*1 + 3] * m[4*2 + 2] * m[4*3 + 0]
								- m[4*1 + 0] * m[4*2 + 3] * m[4*3 + 2]
										- m[4*1 + 2] * m[4*2 + 0] * m[4*3 + 3]);
		f += m[4*0 + 2]
				* ((m[4*1 + 0] * m[4*2 + 1] * m[4*3 + 3] + m[4*1 + 1] * m[4*2 + 3] * m[4*3 + 0] + m[4*1 + 3] * m[4*2 + 0] * m[4*3 + 1])
						- m[4*1 + 3] * m[4*2 + 1] * m[4*3 + 0]
								- m[4*1 + 0] * m[4*2 + 3] * m[4*3 + 1]
										- m[4*1 + 1] * m[4*2 + 0] * m[4*3 + 3]);
		f -= m[4*0 + 3]
				* ((m[4*1 + 0] * m[4*2 + 1] * m[4*3 + 2] + m[4*1 + 1] * m[4*2 + 2] * m[4*3 + 0] + m[4*1 + 2] * m[4*2 + 0] * m[4*3 + 1])
						- m[4*1 + 2] * m[4*2 + 1] * m[4*3 + 0]
								- m[4*1 + 0] * m[4*2 + 2] * m[4*3 + 1]
										- m[4*1 + 1] * m[4*2 + 0] * m[4*3 + 2]);
		return f;
	}

	/**
	 * Calculate the determinant of a 3x3 matrix
	 * @return result
	 */

	private static float determinant3x3(float t00, float t01, float t02,
			float t10, float t11, float t12,
			float t20, float t21, float t22)
	{
		return   t00 * (t11 * t22 - t12 * t21)
				+ t01 * (t12 * t20 - t10 * t22)
				+ t02 * (t10 * t21 - t11 * t20);
	}

	/**
	 * Invert this matrix
	 * @return this if successful, null otherwise
	 */
	public GXMatrix invert() {
		return invert(this, this);
	}

	/**
	 * Invert the source matrix and put the result in the destination
	 * @param src the source matrix
	 * @param dest the destination matrix, or null if a new matrix is to be created
	 * @return the inverted matrix if successful, null otherwise
	 */
	public static GXMatrix invert(GXMatrix src, GXMatrix dest) {
		float determinant = src.determinant();

		if (determinant != 0) {
			/*
			 * m[4*0 + 0] m[4*0 + 1] m[4*0 + 2] m[4*0 + 3]
			 * m[4*1 + 0] m[4*1 + 1] m[4*1 + 2] m[4*1 + 3]
			 * m[4*2 + 0] m[4*2 + 1] m[4*2 + 2] m[4*2 + 3]
			 * m[4*3 + 0] m[4*3 + 1] m[4*3 + 2] m[4*3 + 3]
			 */
			if (dest == null)
				dest = new GXMatrix();
			float determinant_inv = 1f/determinant;

			// first row
			float[] t = new float[16];
			t[4*0 + 0] =  determinant3x3(
					src.m[4*1 + 1], src.m[4*1 + 2], src.m[4*1 + 3],
					src.m[4*2 + 1], src.m[4*2 + 2], src.m[4*2 + 3],
					src.m[4*3 + 1], src.m[4*3 + 2], src.m[4*3 + 3]);
			t[4*0 + 1] = -determinant3x3(src.m[4*1 + 0], src.m[4*1 + 2], src.m[4*1 + 3], src.m[4*2 + 0], src.m[4*2 + 2], src.m[4*2 + 3], src.m[4*3 + 0], src.m[4*3 + 2], src.m[4*3 + 3]);
			t[4*0 + 2] =  determinant3x3(src.m[4*1 + 0], src.m[4*1 + 1], src.m[4*1 + 3], src.m[4*2 + 0], src.m[4*2 + 1], src.m[4*2 + 3], src.m[4*3 + 0], src.m[4*3 + 1], src.m[4*3 + 3]);
			t[4*0 + 3] = -determinant3x3(src.m[4*1 + 0], src.m[4*1 + 1], src.m[4*1 + 2], src.m[4*2 + 0], src.m[4*2 + 1], src.m[4*2 + 2], src.m[4*3 + 0], src.m[4*3 + 1], src.m[4*3 + 2]);
			// second row
			t[4*1 + 0] = -determinant3x3(src.m[4*0 + 1], src.m[4*0 + 2], src.m[4*0 + 3], src.m[4*2 + 1], src.m[4*2 + 2], src.m[4*2 + 3], src.m[4*3 + 1], src.m[4*3 + 2], src.m[4*3 + 3]);
			t[4*1 + 1] =  determinant3x3(src.m[4*0 + 0], src.m[4*0 + 2], src.m[4*0 + 3], src.m[4*2 + 0], src.m[4*2 + 2], src.m[4*2 + 3], src.m[4*3 + 0], src.m[4*3 + 2], src.m[4*3 + 3]);
			t[4*1 + 2] = -determinant3x3(src.m[4*0 + 0], src.m[4*0 + 1], src.m[4*0 + 3], src.m[4*2 + 0], src.m[4*2 + 1], src.m[4*2 + 3], src.m[4*3 + 0], src.m[4*3 + 1], src.m[4*3 + 3]);
			t[4*1 + 3] =  determinant3x3(src.m[4*0 + 0], src.m[4*0 + 1], src.m[4*0 + 2], src.m[4*2 + 0], src.m[4*2 + 1], src.m[4*2 + 2], src.m[4*3 + 0], src.m[4*3 + 1], src.m[4*3 + 2]);
			// third row
			t[4*2 + 0] =  determinant3x3(src.m[4*0 + 1], src.m[4*0 + 2], src.m[4*0 + 3], src.m[4*1 + 1], src.m[4*1 + 2], src.m[4*1 + 3], src.m[4*3 + 1], src.m[4*3 + 2], src.m[4*3 + 3]);
			t[4*2 + 1] = -determinant3x3(src.m[4*0 + 0], src.m[4*0 + 2], src.m[4*0 + 3], src.m[4*1 + 0], src.m[4*1 + 2], src.m[4*1 + 3], src.m[4*3 + 0], src.m[4*3 + 2], src.m[4*3 + 3]);
			t[4*2 + 2] =  determinant3x3(src.m[4*0 + 0], src.m[4*0 + 1], src.m[4*0 + 3], src.m[4*1 + 0], src.m[4*1 + 1], src.m[4*1 + 3], src.m[4*3 + 0], src.m[4*3 + 1], src.m[4*3 + 3]);
			t[4*2 + 3] = -determinant3x3(src.m[4*0 + 0], src.m[4*0 + 1], src.m[4*0 + 2], src.m[4*1 + 0], src.m[4*1 + 1], src.m[4*1 + 2], src.m[4*3 + 0], src.m[4*3 + 1], src.m[4*3 + 2]);
			// fourth row
			t[4*3 + 0] = -determinant3x3(src.m[4*0 + 1], src.m[4*0 + 2], src.m[4*0 + 3], src.m[4*1 + 1], src.m[4*1 + 2], src.m[4*1 + 3], src.m[4*2 + 1], src.m[4*2 + 2], src.m[4*2 + 3]);
			t[4*3 + 1] =  determinant3x3(src.m[4*0 + 0], src.m[4*0 + 2], src.m[4*0 + 3], src.m[4*1 + 0], src.m[4*1 + 2], src.m[4*1 + 3], src.m[4*2 + 0], src.m[4*2 + 2], src.m[4*2 + 3]);
			t[4*3 + 2] = -determinant3x3(src.m[4*0 + 0], src.m[4*0 + 1], src.m[4*0 + 3], src.m[4*1 + 0], src.m[4*1 + 1], src.m[4*1 + 3], src.m[4*2 + 0], src.m[4*2 + 1], src.m[4*2 + 3]);
			t[4*3 + 3] =  determinant3x3(src.m[4*0 + 0], src.m[4*0 + 1], src.m[4*0 + 2], src.m[4*1 + 0], src.m[4*1 + 1], src.m[4*1 + 2], src.m[4*2 + 0], src.m[4*2 + 1], src.m[4*2 + 2]);

			// transpose and divide by the determinant
			for(int i = 0; i < 4; i++)
				for(int j = 0; j < 4; j++)
					dest.m[4*i + j] = t[4*j + i]*determinant_inv;

			return dest;
		} else
			return null;
	}

	/**
	 * negate this matrix
	 * @return this
	 */
	public GXMatrix negate() {
		return negate(this);
	}

	/**
	 * negate this matrix and place the result in a destination matrix.
	 * @param dest the destination matrix, or null if a new matrix is to be created
	 * @return the negated matrix
	 */
	public GXMatrix negate(GXMatrix dest) {
		return negate(this, this);
	}

	/**
	 * negate this matrix and place the result in a destination matrix.
	 * @param src the source matrix
	 * @param dest the destination matrix, or null if a new matrix is to be created
	 * @return the negated matrix
	 */
	public static GXMatrix negate(GXMatrix src, GXMatrix dest) {
		if (dest == null)
			dest = new GXMatrix();

		for(int i = 0; i < 16; i++)
			dest.m[i] = -src.m[i];

		return dest;
	}
}