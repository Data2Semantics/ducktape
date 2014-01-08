from ducktape import *

@main(str,str,str)
def hello(name,origin):
    """ This is the first hello world for python function/modules """
    return "helo " + str.strip(str(name)) +" van "+ str.strip(str(origin))


print hello("Zen","Japan")
