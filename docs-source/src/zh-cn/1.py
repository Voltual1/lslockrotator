import os
import shutil
from pathlib import Path

def collect_md_files():
    """将当前目录下所有子文件夹中的.md文件复制到新建的md文件夹中（平铺）"""
    current_dir = Path.cwd()
    target_dir = current_dir / "md"
    
    # 创建目标文件夹（如果不存在）
    target_dir.mkdir(exist_ok=True)
    
    # 记录复制结果
    copied_count = 0
    skipped_count = 0
    
    # 遍历当前目录下的所有子文件夹（不包括目标文件夹本身）
    for item in current_dir.iterdir():
        if item.is_dir() and item.name != "md":
            # 查找子文件夹中的所有.md文件
            for md_file in item.glob("*.md"):
                # 生成目标文件路径
                target_path = target_dir / md_file.name
                
                # 处理文件名冲突：如果已存在同名文件，则添加前缀或重命名
                if target_path.exists():
                    base = md_file.stem
                    suffix = md_file.suffix
                    counter = 1
                    while target_path.exists():
                        new_name = f"{base}_{counter}{suffix}"
                        target_path = target_dir / new_name
                        counter += 1
                    print(f"警告：文件 {md_file.name} 已存在，已重命名为 {target_path.name}")
                
                try:
                    shutil.copy2(md_file, target_path)  # copy2保留元数据
                    print(f"已复制：{md_file} -> {target_path}")
                    copied_count += 1
                except Exception as e:
                    print(f"复制失败：{md_file}，错误：{e}")
                    skipped_count += 1
                    
    print(f"\n处理完成！共复制 {copied_count} 个文件，跳过 {skipped_count} 个文件。")
    print(f"所有文件已平铺至：{target_dir.absolute()}")

if __name__ == "__main__":
    collect_md_files()