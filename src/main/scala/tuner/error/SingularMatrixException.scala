package tuner.error


class SingularMatrixException(val mtx:breeze.linalg.DenseMatrix[Double]) extends Exception("The matrix is singular")

