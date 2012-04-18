
file_name = ARGV[0]
text  = `cat #{file_name}`
lines = text.split("\n")
rows  = lines.collect{|l| l.split(",")}
speeds = rows.collect{|r| r[2].to_f}

sum = 0
max = 0
speeds.each do |speed|
	sum += speed
	if(speed > max)
		max = speed
	end
end

average = sum / speeds.length

average_millis = average / 1000000

speed_size_pairs = rows.collect{|r| [r[2].to_f,r[3].to_i]}

File.open("plots_" + file_name,"w") do |file|
  speed_size_pairs.each do |pair|
    file.write(pair[0].to_s + ", " + pair[1].to_s + "\n")
  end 
end

