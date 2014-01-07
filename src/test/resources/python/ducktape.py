import msgpack

# Decorators for converting existing python function into ductape modules.
# What needs to be indicated is the input types and the output types of each module.

def main(*types): 

    def decorator(original_function):

	def wrapper(**args):
	    # types include output
	    print "Reading dumps for all function arguments ", args
	    assert len(types) == len(args)+1

	    #For each input.	   
	    output = original_function(**args)
	    print "Dumping output ", output

	    return output


	return wrapper


    return decorator


# Decorator which turns ordinary functions into keyword argument function
# If the python method already all use keyword arguments then this is not needed.
def inputs(*names):

    def decorator(original_function):
        
	def wrapper(*args):
	    assert len(names) == len(args)

	    kwargs = {}
	    for i in range(len(args)):
		kwargs[names[i]] = args[i]
	    print kwargs
	    return original_function(**kwargs)

        return wrapper	

    return decorator

