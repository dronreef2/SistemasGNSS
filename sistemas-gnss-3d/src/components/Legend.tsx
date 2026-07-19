import { GROUP_CONFIGS } from '../types';

export function Legend() {
  return (
    <div className="absolute bottom-6 right-6 bg-[#0a0f1a]/80 backdrop-blur-xl border border-white/5 rounded-xl p-4">
      <h4 className="text-[10px] font-semibold text-white/30 uppercase tracking-wider mb-2">
        Legenda
      </h4>
      <div className="space-y-1.5">
        {GROUP_CONFIGS.map((group) => (
          <div key={group.key} className="flex items-center gap-2">
            <div
              className="w-2 h-2 rounded-full"
              style={{ backgroundColor: group.color }}
            />
            <span className="text-xs text-white/60">{group.label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
