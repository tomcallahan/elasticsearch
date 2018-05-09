#!/usr/bin/env python
from packaging import version
import os
import json
import sys

if len(sys.argv) == 1:
  raise Exception('what version?  we could get this from version.properties...')

branch_version = version.parse(sys.argv[1].split('-')[0])
print("# "+str(branch_version)+" changelog")

changelog_files = []
for f in os.listdir("changelog"):
  f = "changelog/"+f
  if os.path.isfile(f) and f.endswith(".json"):
    with open(f, 'r') as changelog_file:
      entry = json.load(changelog_file)
      smallest_version = sorted(list(map(version.parse, entry.get('intended_versions'))))[0]
      if smallest_version == branch_version:
        breaking = "BREAKING: " if entry.get('breaking') else ""
	headline = entry.get('headline')
	pull_request = entry.get('pull_request')
	print(breaking + headline + "  [(" + pull_request + ")](" + "https://github.com/elastic/elasticsearch/" + pull_request + ")\n")
