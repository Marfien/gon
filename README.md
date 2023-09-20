# GON - General Object Notation
## Abstract
Gon tries to combine the best of YAML, JSON and XML by providing 
readability, performance and flexibility.

## The syntax
Gon provides in its syntax a way of defining a name, class and attributes and flags for each object or node.
Yet, all of them are optional, to be easily migratable from any other configuration.

### Names
Names must not contain any special characters. Characters must be either a letter or a digit.

### Simple values
Simple values are numbers, booleans and strings. They have to be declared the following way:
- Numbers: Can be written as a decimal number or as hex code. As soon as a `.` is found it is classified as a float number.
Otherwise, it is classified as an integer
- Booleans: Might be `true` or `false`
- Strings: String are declared by surrounding them with `"`. E.g. `"This is a String"`.
To escape any character, put a `\ ` in front of it.

### Node header
```
name[class] attribute="value" -flag
```
Explanation: \
`name`: The name or key (name) \
`class`: 
the class. Comparable to the tag in XML. Same restrictions as in `name`. \
`attribute`: The key/identifier of an attribute (name) \
`"value"`: The value of an attribute (simple value) \
`flag`: the name of the flag. Put a `!` to negate it (handled as boolean attribute)

### Node value
A node can have one of the three value declarations: 
- No value:
  ```
  name[NoValue];
  ```
- Simple value
  ```
  name[SimpleValue]: "This is a simple value";
  ```
- Nested nodes
  ```
  name[NestedNodes] {
    anotherNode[NoValue];
    simple[SimpleValue]: 0x0F0F0F;
  }
  ```
