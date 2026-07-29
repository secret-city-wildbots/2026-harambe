import { h, Fragment } from "preact";

interface ChecklistItemProps {
    text: string;
}

const ChecklistItem = ({ text }: ChecklistItemProps) => {
    const id = text + Math.round(Math.random() * 99);
    return (
        <li class="bubble checklist-item">
            <div class="form-check">
                <input
                    class="form-check-input"
                    type="checkbox"
                    value=""
                    id={id}
                />
                <label class="form-check-label" for={id}>
                    {text}
                </label>
            </div>
        </li>
    );
};

export default ChecklistItem;
