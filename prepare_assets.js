const fs = require('fs');
const path = require('path');

const DATASET_DIR = '../temp-dataset';
const ASSETS_DIR = './app/src/main/assets';

// 创建 assets 目录
if (!fs.existsSync(ASSETS_DIR)) {
    fs.mkdirSync(ASSETS_DIR, { recursive: true });
}

// 读取练习数据
const exercises = JSON.parse(fs.readFileSync(path.join(DATASET_DIR, 'data/exercises.json'), 'utf8'));

// 筛选用户需要的器材
const equipmentFilter = ['body weight', 'roller', 'wheel roller', 'rope'];
const filtered = exercises.filter(e => equipmentFilter.includes(e.equipment));

console.log(`筛选前: ${exercises.length} 个练习`);
console.log(`筛选后: ${filtered.length} 个练习`);

// 保存筛选后的数据
fs.writeFileSync(path.join(ASSETS_DIR, 'exercises.json'), JSON.stringify(filtered, null, 2));

// 复制 GIF 文件
const videosDir = path.join(DATASET_DIR, 'videos');
const imagesDir = path.join(ASSETS_DIR, 'images');

if (!fs.existsSync(imagesDir)) {
    fs.mkdirSync(imagesDir, { recursive: true });
}

let copiedCount = 0;
filtered.forEach(exercise => {
    const gifFile = path.join(videosDir, path.basename(exercise.gif_url));
    const destFile = path.join(imagesDir, path.basename(exercise.gif_url));

    if (fs.existsSync(gifFile)) {
        fs.copyFileSync(gifFile, destFile);
        copiedCount++;
    } else {
        console.log(`未找到: ${exercise.gif_url}`);
    }
});

console.log(`复制了 ${copiedCount} 个 GIF 文件`);
console.log('Assets 准备完成！');
