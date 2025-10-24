import os

for root, dirs, files in os.walk('.'):
    for file in files:
        if file.endswith('.java'):
            path = os.path.join(root, file)
            
            with open(path, 'r', encoding='utf-8') as f:
                lines = f.readlines()
            
            # Удаляем строки с комментариями //
            new_lines = []
            for line in lines:
                if '//' in line:
                    # Берем только часть до комментария
                    cleaned = line.split('//')[0].rstrip()
                    if cleaned:  # Если что-то осталось после удаления комментария
                        new_lines.append(cleaned + '\n')
                else:
                    new_lines.append(line)
            
            with open(path, 'w', encoding='utf-8') as f:
                f.writelines(new_lines)
            
            print(f"Обработан: {path}")
