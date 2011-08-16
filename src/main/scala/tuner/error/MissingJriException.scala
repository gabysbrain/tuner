package tuner.error

class MissingJriException(cause:Throwable) 
        extends Exception("JRI library not found", cause) {
}

