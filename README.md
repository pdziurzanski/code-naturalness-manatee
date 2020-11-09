# code-naturalness-manatee

This software tool creates a database (in the SQLite format) with n-grams models 
of abstracted Java sources. The input source codes are stored in an SQLite database. In the current version, the database from the following paper is used.


E. A. Santos, J. C. Campbell, D. Patel, A. Hindle and J. N. Amaral, "Syntax and sensibility: Using language models to detect and correct syntax errors," 2018 IEEE 25th International Conference on Software Analysis, Evolution and Reengineering (SANER), Campobasso, 2018, pp. 311-322, doi: 10.1109/SANER.2018.8330219.

In that database, the codes are stored in the table named 'source_summary' with two relevant fields: 'hash' and 'source'. The 'source' field stores the entire Java files.

The concrete Java codes from that database are firstly abstractised to another database with only one table named 'abstracted_source_files' and two relevant fields: 'hash' and 'source'. In the abstracted version, all identifiers are treated as the same identifier token. The same abstraction is applied to numeric literals and string literals. The location of the databases can be configured in file 'config.properties'.


At the next stage, the n-gram models from the abstracted souce codes are built. The value of n is set as a parameter in file 'config.properties'. The n-gram model is stored in a database, in the table named 'ngrams'. The subseqent (abstracised) tokens are stored in fields: 's1', 's2', ..., 'sn', whereas the number of occurrences of each n-gram is stored in field 'count', and its occurrence probability are stored in field 'probability'.  

The authors acknowledge the support of the EPSRC-funded MANATEE project. 
