const path = require('path');

module.exports = {
  entry: './app/src/main/assets/app.js',
  output: {
    filename: 'app.bundle.js',
    path: path.resolve(__dirname, 'app/src/main/assets'),
  },
  mode: 'production',
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env']
          }
        }
      }
    ]
  },
  resolve: {
    extensions: ['.js', '.json']
  }
};
