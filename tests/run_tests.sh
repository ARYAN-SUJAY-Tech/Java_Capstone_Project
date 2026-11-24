#!/bin/bash
# Compiles the project and diffs every tests/<name>.txt against tests/<name>_expected.txt
cd "$(dirname "$0")/.." || exit 1
rm -rf out && javac -d out src/*.java || exit 1
fail=0
for input in tests/*.txt; do
    case "$input" in *_expected.txt) continue;; esac
    expected="${input%.txt}_expected.txt"
    if java -cp out Main < "$input" | diff - "$expected" > /dev/null; then
        echo "PASS $input"
    else
        echo "FAIL $input"; java -cp out Main < "$input" | diff - "$expected"; fail=1
    fi
done
exit $fail
