import ducktape

@ducktape.main(str,str)
def helloWorld(name):
    return "Hello " + str(name) 


def undecorated(func):
    print "I am not ducktape module"
