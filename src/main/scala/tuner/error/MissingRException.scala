package tuner.error

class MissingRException(cause:Throwable) 
        extends Exception("R not found", cause) {
}

