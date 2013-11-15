#! /usr/bin/env ruby

require 'csv'

def rosenbrock(inputs)
  sum = 0
  inputs.each_cons(2) do |x1, x2|
    sum += (1-x1)**2 + 100 * (x2 - x1**2)**2
  end
  sum
end

input = CSV.read(ARGV[0], {:headers => true})
output = CSV.open(ARGV[1], "wb")

fields = input.headers.sort
output << fields + ["y"]

input.each do |row|
  input_vals = row.fields(*fields)
  response = rosenbrock(input_vals.map {|x| x.to_f})
  output << input_vals + [response]
end

output.close

