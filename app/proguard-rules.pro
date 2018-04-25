
-dontpreverify


-optimizations !code/simplification/arithmetic,method/removal/parameter,method/inlining/*,code/removal/*
-optimizationpasses 5
-allowaccessmodification
-keepattributes *Annotation* ,Signature, !LocalVariableTable,!LocalVariableTypeTable,LineNumberTable,SourceFile,EnclosingMethod


-dontwarn org.joda.time.**
