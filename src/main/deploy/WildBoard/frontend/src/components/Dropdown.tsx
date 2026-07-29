import { h } from "preact";
import { useState, useEffect } from "preact/hooks";

interface DropdownProps {
    label?: string;
    items: string[];
    valSetter: (newVal: string) => void;
}

const Dropdown = ({ label, items, valSetter }: DropdownProps) => {
    const [isOpen, setIsOpen] = useState(false);
    const [selectedLabel, setSelectedLabel] = useState(label || items[0]);

    const toggleMenu = () => setIsOpen((prev) => !prev);

    const handleItemClick = (item: string) => {
        setSelectedLabel(item);
        setIsOpen(false);
    };

    // Only call valSetter when selectedLabel changes
    useEffect(() => {
        valSetter(selectedLabel);
    }, [selectedLabel]);

    // Click outside to close
    useEffect(() => {
        const handler = (e: MouseEvent) => {
            const target = e.target as HTMLElement;
            if (!target.closest(".dropdown")) {
                setIsOpen(false);
            }
        };
        document.addEventListener("click", handler);
        return () => document.removeEventListener("click", handler);
    }, []);

    return (
        <div class="dropdown">
            <button
                class="btn dropdown-toggle"
                type="button"
                onClick={toggleMenu}
                aria-expanded={isOpen}
            >
                {selectedLabel}
            </button>
            <ul class={`dropdown-menu${isOpen ? " show" : ""}`}>
                {items.map((item, index) => (
                    <li key={index}>
                        <button
                            class={
                                "dropdown-item" +
                                (item === selectedLabel ? " active" : "")
                            }
                            onClick={() => handleItemClick(item)}
                        >
                            {item}
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Dropdown;
