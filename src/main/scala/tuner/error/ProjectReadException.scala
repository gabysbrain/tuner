package tuner.error

class ProjectLoadException(val msg:String, cause:Exception) 
        extends Exception(msg, cause)

