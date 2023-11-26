package WdPRiRLabs.Exercises.Ex1;

public class Complex {
        private double real = 0.0;
        private double imag = 0.0;

        public void setRe(double re) {
            real = re;
        }

        public void setIm(double im) {
            imag = im;
        }

        public double getRe() {
            return real;
        }

        public double getIm() {
            return imag;
        }

        public Complex(double re, double im) {
            setIm(im);
            setRe(re);
        }

        public Complex conj() {
            return new Complex(real, -imag);
        }

        public Complex times(Complex c) {
            return new Complex(
                getRe()*c.getRe()-getIm()*c.getIm(),
                getRe()*c.getIm()+getIm()*c.getRe()
            );
        }

        public Complex plus(Complex c) {
            return new Complex(getRe()+c.getRe(), getIm()+c.getIm());
        }

        public Complex minus(Complex c) {
            return new Complex(getRe()-c.getRe(), getIm()-c.getIm());
        }

        public Complex unaryMinus(Complex c) {
            return new Complex(-c.getRe(), -c.getIm());
        }

        public double mag() {
            return Math.sqrt(getRe()*getRe() + getIm()*getIm());
        }
    }
