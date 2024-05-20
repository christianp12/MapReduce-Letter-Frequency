package it.unipi.hadoop;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class LetterFrequency
{
    public static class TextMapper extends Mapper<Object, Text, Text, IntWritable> 
    {
        private Text reducerKey = new Text();
        private final static IntWritable reducerValue = new IntWritable(1);

        public void setup(Context context)
        {
            // Configuration
        }

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException 
        {   
            // Convert the line to lower case
            String line = value.toString().toLowerCase();

            for (char ch : line.toCharArray()) {
                // Check if the character is a letter
                if (Character.isLetter(ch)) { 
                    reducerKey.set(String.valueOf(ch));
                    context.write(reducerKey, reducerValue);
                }
            }
        }
    }

    public static class TextReducer extends Reducer<Text, IntWritable, Text, IntWritable>
    {
        // Variables

        public void setup(Context context)
        {
            // Configuration
        }

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException 
        {
            // Variables
            int sum = 0;

            // Iterate over the values
            for (IntWritable value : values) {
                sum += value.get();
            }

            // Write the output
            context.write(key, new Text(String.valueOf(sum)));
         
            
        }
    }

    public static void main(String[] args) throws Exception
    {

        // Default values
        final int DEFAULT_NUM_REDUCERS = 1;

        // Configuration of the job
        Configuration conf = new Configuration();

        Map<String, String> argMap = new HashMap<>();

        for (String arg : args) {
            String[] parts = arg.split("=");
            if (parts.length == 2) {
                argMap.put(parts[0], parts[1]);
            } else {
                System.err.println("Invalid argument: " + arg);
                System.exit(1);
            }
        }

        if (!argMap.containsKey("input") || !argMap.containsKey("output")) {
            System.err.println("Usage: LetterFrequency input=<input> output=<output> [numReducers=<num of reducer tasks>]");
            System.exit(1);
        }

        System.out.println("args[0]: <input>="  + argMap.get("input"));
        System.out.println("args[1]: <output>=" + argMap.get("output"));

        // Create a new Job
        Job job = Job.getInstance(conf, "LetterFrequency");

        // Set configuration parameters
        // job.getConfiguration().set("config_var", "value");

        // Set the main classes
        job.setJarByClass(LetterFrequency.class);
        job.setMapperClass(TextMapper.class);
        job.setReducerClass(TextReducer.class);

        // Set the combiner class
        job.setCombinerClass(TextReducer.class);

        // Set number of reducers 
        if (argMap.containsKey("numReducers")) {
            job.setNumReduceTasks(Integer.parseInt(argMap.get("numReducers")));
        }else{
            job.setNumReduceTasks(DEFAULT_NUM_REDUCERS);
        }
    
        // Set the output key and value classes
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // Set the input and output paths
        FileInputFormat.addInputPath(job, new Path(argMap.get("input")));
        FileOutputFormat.setOutputPath(job, new Path(argMap.get("output")));

        // Set the input and output formats
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // Exit
        System.exit(job.waitForCompletion(true) ? 0 : 1);
     }
}
