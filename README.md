# Jarpa – Java ARguments PArser

A lightweight parser of command line arguments.

## Features

* Very lightweight
* Only throws `JarpaException`s with friendly error messages – you can catch them and show the user the message
* Completely typesafe – handles parsing for you
* Extensible in the previous regard

## Quick Examples

Note that the following examples use the static import `fi.purkka.jarpa.JarpaArg.*` to make the code shorter.

Everything begins by obtaining a `JarpaArgs` instance; `args` is of type `String[]`. `JarpaArgs` implements `AutoCloseable` to permit use in try-catch blocks; the `close()` method calls `finish()` which makes sure that the user hasn't entered any unknown arguments.

    try(JarpaArgs jargs = JarpaParser.parsing(args).parse()) {
        // code
    } catch(JarpaException e) {
        // handle errors
    }

`JarpaArgs` has a `get()` method that takes a `JarpaArg`. The static methods of `JarpaArg` provide facilities for obtaining values of different types.

    String string = jargs.get(string("--name"));
    int integer = jargs.get(integer("--id"));
    double decimal = jargs.get(decimal("--weight"));
    String[] strings = jargs.get(stringArray("--versions"));

*Flags* are special types of `JarpaArg`. They are associated with a `boolean` value that indicates whether they are present. Unlike other types, they will not cause an exception to be thrown if missing).

    boolean hasFlag = jargs.get(flag("-f"));

Kind of like flags, *optional arguments* can freely be omitted. They provide access to an `Optional` instance.

    int repeat = jargs.get(integer("--repeat").optional()).orElse(1);

*Aliases* can be used to allow the user use shorter or longer versions of arguments as they wish. Only one given alias may be used at the same time; otherwise, an exception is raised.

    boolean verbose = jargs.get(flag("--verbose").alias("-v"));

Other types of `JarpaArg` can be used if a parse method is provided.

    MyClass myClass = jargs.get(object("--myclass", MyClass::parse));

## Licence

Jarpa is licenced with the Unlicence.
