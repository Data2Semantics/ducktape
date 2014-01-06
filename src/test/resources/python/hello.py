def helloWorld():
    global name, greetings
    
    greetings = "Hello world " + name
    return greetings

print "Check ", name, greetings
helloWorld();

print "Check ", name, greetings