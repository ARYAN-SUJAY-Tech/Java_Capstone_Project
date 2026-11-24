# Library Management System (Java Capstone)

## Build and run
    javac -d out src/*.java
    java -cp out Main < tests/sample.txt > out.txt
    diff out.txt tests/sample_expected.txt

## Run all tests
    ./tests/run_tests.sh

Each `tests/<name>.txt` is compared against `tests/<name>_expected.txt`:
- `sample` - the sample session from the brief
- `errors` - one case for each error in section 7
- `limits` - a Student, a Staff member and a Faculty member each reach their limit
- `reserve` - the RESERVE bonus feature
