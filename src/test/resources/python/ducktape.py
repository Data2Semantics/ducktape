import msgpack
import inspect
import functools
# Decorators for converting existing python function into ductape modules.
# What needs to be indicated is the input types and the output types of each module.

module_registry = {}

class DucktapeModule(object):
	def __init__(self, name):
		self.name = name;


def main(*types): 

    def decorator(original_function):

	# What I wanted here is that this function 
	fspec = inspect.getargspec(original_function)

	# input names are actually arg names
	inputnames = fspec.args

	moduleName = original_function.__name__

	module_registry[moduleName] = {} 

	module_registry[moduleName]["name"] = moduleName 
	module_registry[moduleName]["description"] = original_function.__doc__ 

	module_registry[moduleName]["inputs"] = inputnames
	module_registry[moduleName]["input_types"] = types



	@functools.wraps(original_function)
        def wrapper(*args):
            # types include output
            assert len(types) == len(args)+1

	    #try to open file for each argnames and assigned it to args.
	    #if these inputs somehow are not available, then return original function
	    try :
		newargs = [];
		for f_input in inputnames :
			fi = open (f_input)
			value = msgpack.unpack(fi.read())
			newargs.append( value )


		# Call the function using this.           
		output = original_function(*newargs)

		fo = open(original_function.__name__, "w")
		fo.write(msgpack.pack(output))
		fo.close()

		
		return output

	    except:
		return original_function(*args)

        return wrapper

    return decorator 

