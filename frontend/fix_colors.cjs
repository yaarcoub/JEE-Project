const fs = require("fs");
const path = require("path");
function walk(dir) {
  let results = [];
  const list = fs.readdirSync(dir);
  list.forEach((file) => {
    file = path.join(dir, file);
    const stat = fs.statSync(file);
    if (stat && stat.isDirectory()) {
      results = results.concat(walk(file));
    } else if (file.endsWith(".tsx")) {
      results.push(file);
    }
  });
  return results;
}

const files = walk("c:/Users/abdel/OneDrive/Desktop/JEE-Project/frontend/src");
files.forEach((file) => {
  let content = fs.readFileSync(file, "utf8");
  let original = content;

  // Replace purple with blue
  content = content.replace(/-purple-/g, "-blue-");

  // Replace color: 'white' with color: 'inherit' in DataGrids
  content = content.replace(/color:\s*'white'/g, "color: 'inherit'");

  // Replace column headers background
  content = content.replace(
    /backgroundColor:\s*'rgba\\(0,0,0,0.2\\)'/g,
    "backgroundColor: 'inherit'",
  );
  content = content.replace(/color:\s*'#e2e8f0'/g, "color: 'inherit'");

  // Replace input backgrounds
  content = content.replace(
    /backgroundColor:\s*'rgba\\(255,255,255,0.05\\)'/g,
    "backgroundColor: 'inherit'",
  );

  if (content !== original) {
    fs.writeFileSync(file, content);
    console.log("Updated " + file);
  }
});
