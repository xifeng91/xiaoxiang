package com.marvin;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class HdTest {
	public static class IpCountMapper extends
			Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);
		private Text ip = new Text();

		@Override
		protected void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] strs = value.toString().split(" ");
			ip.set(strs[0]);
			context.write(ip, one);
		}
	}

	public static class IpCountReducer extends
			Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		protected void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {

			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}

	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: ipcount <in> <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "ip count");
		job.setJarByClass(HdTest.class);
		job.setMapperClass(IpCountMapper.class);
		job.setCombinerClass(IpCountReducer.class);
		job.setReducerClass(IpCountReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

		FileSystem fs = FileSystem.get(conf);
		fs.delete(new Path(otherArgs[1]), true);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
