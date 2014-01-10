import msgpack
import inspect
import functools
# Decorators for converting existing python function into ductape modules.
# What needs to be indicated is the input types and the output types of each module.

module_registry = {}

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
        module_registry[moduleName]["input_types"] = [t for t in types[:-1]]

        module_registry[moduleName]["outputs"] = [moduleName]
        module_registry[moduleName]["output_types"] = [o for o in types[-1:]]

        @functools.wraps(original_function)
        def wrapper(*args):

            #try to open file for each argnames and assigned it to args.
            #if these inputs somehow are not available, then return original function
            try :
                newargs = [];
                for f_input in inputnames :
                        fi = open (f_input)
                        value = msgpack.unpackb(fi.read())
                        newargs.append( value )

                # Call the function using this.           
                output = original_function(*newargs)

                fo = open(original_function.__name__, "w")
                fo.write(msgpack.packb(output))
                fo.close()

                
                return output

            except:
                return original_function(*args)

        return wrapper

    return decorator 

def dump_registry () :
    """ Dumping module registry to be used in ducktape python domain to get information about module"""
    f=open('moduleInfo.msg','w')
    for module in module_registry:
        cur = module_registry[module]
        # This list is how msgpack expect MessageInfo object to be passed to java.
        infoaslist = [cur['name'], cur['description'], cur['inputs'], cur['input_types'], cur['outputs'], cur['output_types']]
        f.write(msgpack.packb(infoaslist))
    f.close()
