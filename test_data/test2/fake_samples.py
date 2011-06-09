
from sys import argv
from os import path
from shutil import copyfile
import csv
from random import random
import time

DEFAULTIMAGE = 'img001.jpeg'

if __name__ == '__main__':
  mydirname = path.dirname(argv[0])
  if mydirname == '': mydirname = '.'
  infilename = argv[1]
  outfilename = argv[2]

  print "faking it..."

  infile = csv.reader(open(infilename, 'r'))
  outfile = csv.writer(open(outfilename, 'w'), delimiter='\t')
  header = infile.next()
  outfile.writerow(header + ['resp1'])

  for i, line in enumerate(infile):
    outfile.writerow(line + [random()])
    time.sleep(1)

