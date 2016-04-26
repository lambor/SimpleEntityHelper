# SimpleEntityHelper
A gradle plugin help for [SimpleEntity](https://github.com/lambor/SimpleEntity)

### Extension
```
simpleEntityHelper {
    jsonStringClass = "com.dcnh35.EntityClass"
}
```

`simpleEntityHelper.jsonStringClass` defines class name which holds json strings.

### Tasks
```
gradle compileEntities
```

Task `compileEntities` compile single class defined by `simpleEntityHelper.jsonStringClass`